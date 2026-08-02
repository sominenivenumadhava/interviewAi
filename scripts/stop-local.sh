#!/usr/bin/env bash
# Stop processes started by scripts/run-local.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

stop_pidfile() {
  local file="$1"
  local name="$2"
  if [[ -f "$file" ]]; then
    local pid
    pid="$(cat "$file")"
    if kill -0 "$pid" 2>/dev/null; then
      # Kill process group / children when possible
      pkill -P "$pid" 2>/dev/null || true
      kill "$pid" 2>/dev/null || true
      echo "Stopped $name (pid $pid)"
    fi
    rm -f "$file"
  fi
}

stop_pidfile .local-run/frontend.pid "frontend"
stop_pidfile .local-run/backend.pid "backend"

# Also stop stray vite / spring-boot children for this checkout if still up
pkill -f "${ROOT}/frontend.*vite" 2>/dev/null || true
pkill -f "spring-boot:run" 2>/dev/null || true

if command -v docker >/dev/null 2>&1; then
  docker compose -f docker-compose.dev.yml stop postgres redis 2>/dev/null || true
  echo "Stopped Docker postgres/redis (containers kept)"
fi

echo "Done."
