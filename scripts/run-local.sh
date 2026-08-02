#!/usr/bin/env bash
# Start InterviAI on YOUR machine (not the cloud agent).
# Prerequisites: Java 21+, Maven 3.9+, Node 18+, Docker (for Postgres)
#
# Optional ports (env or flags):
#   FE_PORT=3000 ./scripts/run-local.sh
#   ./scripts/run-local.sh --port 3000
#   ./scripts/run-local.sh --fe-port 3000 --be-port 8082
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

FE_PORT_DEFAULT=5173
BE_PORT_DEFAULT=8082
CLI_FE_PORT=""
CLI_BE_PORT=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    -p|--port|--fe-port)
      CLI_FE_PORT="$2"
      shift 2
      ;;
    --be-port)
      CLI_BE_PORT="$2"
      shift 2
      ;;
    -h|--help)
      echo "Usage: ./scripts/run-local.sh [--port 3000] [--be-port 8082]"
      echo "   or: FE_PORT=3000 ./scripts/run-local.sh"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      echo "Usage: ./scripts/run-local.sh [--port 3000] [--be-port 8082]"
      exit 1
      ;;
  esac
done

need() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo -e "${RED}Missing dependency: $1${NC}"
    echo "Install it, then re-run this script."
    exit 1
  fi
}

need java
need mvn
need node
need npm

if ! command -v docker >/dev/null 2>&1; then
  echo -e "${RED}Docker is required to start Postgres (docker-compose.dev.yml).${NC}"
  echo "Install Docker Desktop, or run Postgres yourself on port 5433 with:"
  echo "  DB: interviai_dev  USER: interviai_user  PASS: interviai_password"
  exit 1
fi

if [[ ! -f .env ]]; then
  cp .env.template .env
  # Match docker-compose.dev.yml host mapping
  if grep -q '^DB_PORT=' .env; then
    sed -i.bak 's/^DB_PORT=.*/DB_PORT=5433/' .env && rm -f .env.bak
  fi
  if grep -q '^DB_PASSWORD=' .env; then
    sed -i.bak 's/^DB_PASSWORD=.*/DB_PASSWORD=interviai_password/' .env && rm -f .env.bak
  fi
  echo -e "${GREEN}Created .env from .env.template (DB_PORT=5433 for Docker Postgres).${NC}"
fi

# Preserve ports passed on the shell before sourcing .env
PRESET_FE_PORT="${FE_PORT:-}"
PRESET_BE_PORT="${BE_PORT:-${SERVER_PORT:-}}"

# Load .env into this shell (safe subset)
set -a
# shellcheck disable=SC1091
source .env
set +a

# Priority: CLI flag > pre-set env > .env > default
FE_PORT="${CLI_FE_PORT:-${PRESET_FE_PORT:-${FE_PORT:-$FE_PORT_DEFAULT}}}"
BE_PORT="${CLI_BE_PORT:-${PRESET_BE_PORT:-${BE_PORT:-${SERVER_PORT:-$BE_PORT_DEFAULT}}}}"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5433}"
export DB_NAME="${DB_NAME:-interviai_dev}"
export DB_USERNAME="${DB_USERNAME:-interviai_user}"
export DB_PASSWORD="${DB_PASSWORD:-interviai_password}"
export JWT_SECRET="${JWT_SECRET:-local-dev-only-jwt-secret-do-not-use-in-prod-404E635266556A586E3272357538782F}"
export SERVER_PORT="$BE_PORT"
export FRONTEND_URL="${FRONTEND_URL:-http://localhost:${FE_PORT}}"

# If FRONTEND_URL still points at another port, refresh it to match FE_PORT
if [[ "$FRONTEND_URL" == http://localhost:* || "$FRONTEND_URL" == http://127.0.0.1:* ]]; then
  export FRONTEND_URL="http://localhost:${FE_PORT}"
fi

mkdir -p "$ROOT/.local-run"

echo -e "${CYAN}▶ Ports: frontend=${FE_PORT}  backend=${BE_PORT}${NC}"
echo -e "${CYAN}▶ Starting Postgres + Redis (Docker)…${NC}"

container_exists() {
  docker ps -a --format '{{.Names}}' | grep -qx "$1"
}

container_running() {
  docker ps --format '{{.Names}}' | grep -qx "$1"
}

# Reuse containers left over from an earlier clone/run (same fixed names).
# Avoids: "Conflict. The container name ... is already in use"
start_or_create_db() {
  local pg=interviai-postgres-dev
  local redis=interviai-redis-dev

  if container_exists "$pg"; then
    if container_running "$pg"; then
      echo "  $pg already running"
    else
      echo "  starting existing $pg"
      docker start "$pg" >/dev/null
    fi
  fi

  if container_exists "$redis"; then
    if container_running "$redis"; then
      echo "  $redis already running"
    else
      echo "  starting existing $redis"
      docker start "$redis" >/dev/null
    fi
  fi

  # Create any that are still missing via compose
  local need_pg=0 need_redis=0
  container_exists "$pg" || need_pg=1
  container_exists "$redis" || need_redis=1

  if [[ $need_pg -eq 1 && $need_redis -eq 1 ]]; then
    docker compose -f docker-compose.dev.yml up -d postgres redis
  elif [[ $need_pg -eq 1 ]]; then
    docker compose -f docker-compose.dev.yml up -d postgres
  elif [[ $need_redis -eq 1 ]]; then
    docker compose -f docker-compose.dev.yml up -d redis
  fi
}

start_or_create_db

echo -e "${CYAN}▶ Waiting for Postgres on localhost:${DB_PORT}…${NC}"
for i in $(seq 1 40); do
  if docker exec interviai-postgres-dev pg_isready -U interviai_user -d interviai_dev >/dev/null 2>&1; then
    break
  fi
  sleep 1
  if [[ $i -eq 40 ]]; then
    echo -e "${RED}Postgres did not become ready in time.${NC}"
    echo "Try: docker start interviai-postgres-dev"
    echo "Or recreate: docker rm -f interviai-postgres-dev interviai-redis-dev && ./scripts/run-local.sh"
    exit 1
  fi
done

if [[ ! -d frontend/node_modules ]]; then
  echo -e "${CYAN}▶ Installing frontend deps (npm ci)…${NC}"
  (cd frontend && npm ci)
fi

# Avoid duplicate backend/frontend if already healthy on the requested ports
if curl -sf "http://127.0.0.1:${BE_PORT}/actuator/health" >/dev/null 2>&1; then
  echo -e "${GREEN}▶ Backend already healthy on :${BE_PORT}${NC}"
else
  echo -e "${CYAN}▶ Starting backend on :${BE_PORT}…${NC}"
  (
    cd backend
    nohup mvn -q spring-boot:run \
      -Dspring-boot.run.profiles=dev \
      -Dspring-boot.run.arguments="--server.port=${BE_PORT}" \
      >"$ROOT/.local-run/backend.log" 2>&1 &
    echo $! >"$ROOT/.local-run/backend.pid"
  )

  echo -e "${CYAN}▶ Waiting for backend health…${NC}"
  for i in $(seq 1 90); do
    if curl -sf "http://127.0.0.1:${BE_PORT}/actuator/health" >/dev/null 2>&1; then
      break
    fi
    sleep 2
    if [[ $i -eq 90 ]]; then
      echo -e "${RED}Backend failed to start. See .local-run/backend.log${NC}"
      tail -n 40 "$ROOT/.local-run/backend.log" || true
      exit 1
    fi
  done
fi

# Point Vite's /api proxy at the chosen backend port for this run
export VITE_DEV_API_TARGET="http://127.0.0.1:${BE_PORT}"

if curl -sf "http://127.0.0.1:${FE_PORT}/" >/dev/null 2>&1; then
  echo -e "${GREEN}▶ Frontend already responding on :${FE_PORT}${NC}"
else
  echo -e "${CYAN}▶ Starting frontend on :${FE_PORT}…${NC}"
  (
    cd frontend
    nohup npm run dev -- --host 127.0.0.1 --port "$FE_PORT" --strictPort \
      >"$ROOT/.local-run/frontend.log" 2>&1 &
    echo $! >"$ROOT/.local-run/frontend.pid"
  )
  sleep 2
fi

# Persist chosen ports for stop script / next time
printf '%s\n' "$FE_PORT" >"$ROOT/.local-run/fe.port"
printf '%s\n' "$BE_PORT" >"$ROOT/.local-run/be.port"

echo ""
echo -e "${GREEN}InterviAI is running on your machine.${NC}"
echo ""
echo "  App:     http://localhost:${FE_PORT}"
echo "  API:     http://localhost:${BE_PORT}"
echo "  Swagger: http://localhost:${BE_PORT}/swagger-ui/index.html"
echo ""
echo "  Logs:    .local-run/backend.log  .local-run/frontend.log"
echo "  Stop:    ./scripts/stop-local.sh"
echo ""
echo "  Tip:     FE_PORT=3000 ./scripts/run-local.sh"
echo "           ./scripts/run-local.sh --port 3000"
echo ""
