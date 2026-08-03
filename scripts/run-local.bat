@echo off
setlocal EnableExtensions
cd /d "%~dp0.."

echo ============================================
echo  InterviAI - start on THIS Windows machine
echo ============================================

where java >nul 2>&1 || (echo [ERROR] Java not found. Install Java 21+ & exit /b 1)
where mvn  >nul 2>&1 || (echo [ERROR] Maven not found. Install Maven 3.9+ & exit /b 1)
where node >nul 2>&1 || (echo [ERROR] Node not found. Install Node 18+ & exit /b 1)
where npm  >nul 2>&1 || (echo [ERROR] npm not found. Install Node.js & exit /b 1)

if not exist ".env" (
  copy /Y ".env.template" ".env" >nul
  echo [OK] Created .env from template — set DEEPGRAM_API_KEY and OPENROUTER_API_KEY
)

REM Prefer Docker Postgres on 5433 when Docker is available
where docker >nul 2>&1
if %ERRORLEVEL%==0 (
  echo [..] Starting Postgres + Redis via docker-compose.dev.yml
  docker compose -f docker-compose.dev.yml up -d postgres redis
  powershell -NoProfile -Command "(Get-Content .env) -replace '^DB_PORT=.*','DB_PORT=5433' | Set-Content .env"
) else (
  echo [WARN] Docker not found. Using local Postgres on DB_PORT from .env
)

if not exist "frontend\node_modules" (
  echo [..] npm ci in frontend
  pushd frontend
  call npm ci
  popd
)

echo [..] Starting BACKEND on http://localhost:8082
start "InterviAI-Backend" cmd /k "cd /d "%cd%\backend" && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

timeout /t 8 /nobreak >nul

echo [..] Starting FRONTEND on http://localhost:5173
start "InterviAI-Frontend" cmd /k "cd /d "%cd%\frontend" && npm run dev -- --host 127.0.0.1 --port 5173"

echo.
echo Open:  http://localhost:5173
echo API:   http://localhost:8082/actuator/health
echo.
echo Two new CMD windows were opened (backend + frontend). Leave them open.
endlocal
