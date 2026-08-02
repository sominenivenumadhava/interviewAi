# InterviAI — Setup Guide

## Prerequisites

- **Java 21+**
- **Node.js 18+** (20 recommended)
- **Maven 3.9+**
- **PostgreSQL 14+** and/or **Docker**

## Ports

| Service | Port |
|---------|------|
| Frontend (Vite) | `5173` |
| Backend | `8082` |
| Postgres (local install) | `5432` |
| Postgres (`docker-compose.dev.yml`) | host `5433` → container `5432` |
| pgAdmin (dev compose) | `5050` |
| Production frontend (compose) | `80` |

## Environment

```bash
cp .env.template .env
```

Fill values from `.env.template`. Important variables:

| Variable | Required | Notes |
|----------|----------|--------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | Yes (backend) | Dev profile defaults: host `localhost`, port **`5433`** |
| `JWT_SECRET` | Yes outside pure local-dev default | Min 256-bit; required for `prod` / `docker` |
| `OPENROUTER_API_KEY` | For AI features | OpenRouter — **not** direct Gemini API |
| `OPENROUTER_MODEL` | Optional | Default `google/gemini-2.5-flash-lite` via OpenRouter |
| `VITE_API_BASE_URL` | Production FE build | Origin only, e.g. `http://localhost:8082` (no `/api/v1`) |
| `FRONTEND_URL` | Optional | Default `http://localhost:5173` |
| `DEEPGRAM_API_KEY` | Optional | Speech; browser STT / mock when unset |
| `APIFY_API_TOKEN` | Optional | Job scrape; disabled when empty |
| `MAIL_*` | Optional | Password reset / notifications |

## Database

### Docker (dev) — recommended when coding against host Maven/npm

```bash
docker compose -f docker-compose.dev.yml up -d postgres
```

Use **`DB_PORT=5433`** for Spring Boot on the host (already the `dev` profile default in `application.yml`).

### Local PostgreSQL

```bash
createdb interviai_dev
# Set DB_PORT=5432 in .env
```

Schema is managed by Liquibase on startup (`classpath:db/changelog/db.changelog-master.xml`).

## Backend

```bash
cd backend
# Ensure .env / env vars are loaded, or export DB_* JWT_SECRET OPENROUTER_API_KEY
mvn spring-boot:run
```

- API: `http://localhost:8082`
- Swagger: `http://localhost:8082/swagger-ui/index.html`

### Tests

```bash
cd backend
mvn test
```

### Package

```bash
cd backend
mvn -DskipTests package
java -jar target/backend-1.0.0.jar
```

## Frontend

```bash
cd frontend
npm ci
npm run dev          # http://localhost:5173 — Vite proxies /api to :8082
npm run build        # production assets in dist/
npm run preview
npm run lint
```

For a production build that talks to a remote/local API:

```bash
export VITE_API_BASE_URL=http://localhost:8082
npm run build
```

## Full Docker stack

Uses `SPRING_PROFILES_ACTIVE=docker` (Postgres hostname `postgres`, JWT from env).

```bash
cp .env.template .env
# Set JWT_SECRET (compose refuses to start without it)
docker compose up -d --build
```

- UI: `http://localhost`
- API: `http://localhost:8082`

Images:

- `backend/Dockerfile` — Maven 3.9 + Temurin 21 build → Temurin 21 JRE
- `frontend/Dockerfile` — `npm ci && npm run build` → `nginx:alpine` + `frontend/nginx.conf` (SPA `try_files`, optional `/api` proxy)

## Implemented modules

1. Common — entities, utilities, exception handling  
2. Authentication — JWT, refresh tokens, password reset  
3. User — registration, profile, preferences  
4. Resume — PDF parse / skills extraction  
5. Interview — sessions, questions, answers  
6. Evaluation — AI assessment and scoring  
7. Analytics — metrics and skill progress  
8. Dashboard — summaries  
9. AI — OpenRouter integration; optional Deepgram speech token endpoint  
10. Jobs — Apify-backed search when configured  
11. Notification — email when SMTP is configured  

## Optional integrations

- **OpenRouter** — all LLM features (questions, evaluation, resume AI, etc.)
- **Deepgram** — optional STT; without a key the app falls back to browser speech / mock token response
- **Apify** — optional live job scrape; empty token disables scrape
- **Mail (SMTP)** — optional; needed for verification / password-reset emails

## Troubleshooting

- **Backend won’t start:** confirm Java 21, Postgres reachable on the port in `DB_PORT`, and `JWT_SECRET` for non-dev profiles
- **AI calls fail:** set `OPENROUTER_API_KEY` from the OpenRouter dashboard
- **Frontend can’t reach API in prod build:** `VITE_API_BASE_URL` must be the origin only (no `/api/v1`)
- **Port conflict on 5432:** use `docker-compose.dev.yml` and `DB_PORT=5433`

## Support

- Issues: repository GitHub Issues  
- Env reference: `.env.template`
