# InterviAI - AI-Powered Interview Platform

## Overview

InterviAI is an AI-powered personalized virtual interview preparation and assessment platform that helps candidates improve their interview skills through realistic AI-driven practice sessions.

## Project Structure

```
├── frontend/          # React + TypeScript (Vite)
├── backend/           # Spring Boot 3.2 (Java 21)
├── docker-compose.yml       # Full stack (Postgres, Redis, backend, frontend)
├── docker-compose.dev.yml   # Dev infra only (Postgres on host port 5433)
├── .env.template            # Environment variable reference
└── README.md
```

## Technology Stack

### Frontend
- React 18 + TypeScript, Vite, Tailwind CSS
- React Router, Framer Motion, Recharts

### Backend
- Java 21, Spring Boot 3.2
- Spring Security + JWT, Spring Data JPA, Liquibase, PostgreSQL
- AI via **OpenRouter** (OpenAI-compatible chat completions; not direct Google Gemini API)
- OpenAPI/Swagger, JUnit 5 / Mockito / Testcontainers

## Ports

| Service | Local default | Notes |
|---------|---------------|--------|
| Frontend (Vite) | `5173` | `npm run dev` |
| Backend | `8082` | Spring Boot `server.port` |
| PostgreSQL (local install) | `5432` | See `.env.template` `DB_PORT` |
| PostgreSQL (`docker-compose.dev.yml`) | host `5433` → container `5432` | Use `DB_PORT=5433` for host apps |
| Frontend (production compose) | `80` | nginx serving `dist` |
| Backend (production compose) | `8082` | |

## Getting Started

### Prerequisites

- Node.js 18+ (20 recommended)
- Java 21+
- Maven 3.9+
- PostgreSQL 14+ **or** Docker for `docker-compose.dev.yml`

### 1. Environment

```bash
cp .env.template .env
# Fill at least: DB_*, JWT_SECRET, OPENROUTER_API_KEY
```

See `.env.template` for the full list (OpenRouter, optional Deepgram/Apify/Mail/OAuth).

### 2. Database

**Option A — Docker (recommended for local apps):**

```bash
docker compose -f docker-compose.dev.yml up -d postgres
# Host apps: DB_PORT=5433  (dev profile default in application.yml)
```

**Option B — Local PostgreSQL on 5432:**

```bash
createdb interviai_dev
# Set DB_PORT=5432 in .env
```

### 3. Backend

```bash
cd backend
mvn spring-boot:run
# or: mvn -DskipTests package && java -jar target/backend-1.0.0.jar
```

API: `http://localhost:8082`  
Swagger: `http://localhost:8082/swagger-ui/index.html`

### 4. Frontend

```bash
cd frontend
npm ci
npm run dev
```

App: `http://localhost:5173`  
Dev API calls go through the Vite proxy to `8082`. For production builds, set `VITE_API_BASE_URL` to the API **origin only** (e.g. `http://localhost:8082`) — do **not** append `/api/v1`.

### Tests

```bash
cd backend && mvn test
cd frontend && npm run lint
```

### Production build (local)

```bash
# Backend JAR
cd backend && mvn -DskipTests package

# Frontend static assets
cd frontend && npm ci && npm run build
```

### Full stack with Docker

```bash
cp .env.template .env   # JWT_SECRET is required (compose fails closed without it)
docker compose up -d --build
```

- Frontend: `http://localhost`
- Backend: `http://localhost:8082`
- Uses Spring profile `docker` (Postgres hostname `postgres`)

## Features (implemented)

- **Common** — base entities, API responses, exception handling
- **Authentication** — JWT, refresh tokens, password reset
- **User** — registration, profile, preferences
- **Resume** — upload/parse (PDF), skills/experience extraction
- **Interview** — sessions, questions, answers
- **Evaluation** — AI scoring and feedback
- **Analytics / Dashboard** — performance metrics and summaries
- **AI** — OpenRouter chat completions; optional speech token endpoint
- **Jobs** — job search via optional Apify scrape
- **Notification** — email (when SMTP configured)

### Optional integrations

| Integration | Env | Behavior when unset |
|-------------|-----|---------------------|
| **OpenRouter** | `OPENROUTER_API_KEY` | Required for AI question/eval features |
| **Deepgram** | `DEEPGRAM_API_KEY` | Browser speech / mock token path used instead |
| **Apify** | `APIFY_API_TOKEN` | Live job scrape disabled |
| **Mail** | `MAIL_USERNAME` / `MAIL_PASSWORD` | Password-reset / notification emails unavailable |

## Development notes

- Default Spring profile: `dev` (DB defaults to `localhost:5433` for compose-dev Postgres)
- `JWT_SECRET`: weak/local default only under `dev` / `test`; `prod` and `docker` require env
- Frontend production base URL: `VITE_API_BASE_URL` (origin only)

## License

MIT — see [LICENSE](LICENSE) if present.
