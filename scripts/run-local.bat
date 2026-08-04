@echo off
setlocal EnableExtensions
cd /d "%~dp0.."

echo ============================================
echo  InterviAI - start on THIS Windows machine
echo  API + Backend = ONE port: 8082
echo ============================================

where java >nul 2>&1 || (echo [ERROR] Java not found. Install Java 21+ & exit /b 1)
where mvn  >nul 2>&1 || (echo [ERROR] Maven not found. Install Maven 3.9+ & exit /b 1)
where node >nul 2>&1 || (echo [ERROR] Node not found. Install Node 18+ & exit /b 1)
where npm  >nul 2>&1 || (echo [ERROR] npm not found. Install Node.js & exit /b 1)

if not exist ".env" (
  copy /Y ".env.template" ".env" >nul
  echo [OK] Created .env from template — set DEEPGRAM_API_KEY and OPENROUTER_API_KEY
)

REM Force single API port (no separate gateway on 8080)
powershell -NoProfile -Command ^
  "$t=Get-Content .env -Raw; $t=$t -replace '(?m)^SERVER_PORT=.*','SERVER_PORT=8082'; $t=$t -replace '(?m)^VITE_API_BASE_URL=.*','VITE_API_BASE_URL=http://localhost:8082'; if($t -notmatch '(?m)^SERVER_PORT='){$t+=\"`nSERVER_PORT=8082`n\"}; if($t -notmatch '(?m)^VITE_API_BASE_URL='){$t+=\"`nVITE_API_BASE_URL=http://localhost:8082`n\"}; Set-Content .env $t -NoNewline"

REM Free 8080 / 8082 / 5173 if still occupied
powershell -NoProfile -Command ^
  "foreach($port in 8080,8082,5173){ Get-NetTCPConnection -LocalPort $port -EA SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -EA SilentlyContinue } }"

REM Prefer Docker Postgres on 5433 when Docker is available
where docker >nul 2>&1
if %ERRORLEVEL%==0 (
  echo [..] Starting Postgres + Redis via docker-compose.dev.yml
  docker compose -f docker-compose.dev.yml up -d postgres redis
  powershell -NoProfile -Command "(Get-Content .env) -replace '^DB_PORT=.*','DB_PORT=5433' -replace '^DB_PASSWORD=.*','DB_PASSWORD=interviai_password' | Set-Content .env"
) else (
  echo [WARN] Docker not found. Using local Postgres on DB_PORT from .env
)

if not exist "frontend\node_modules" (
  echo [..] npm ci in frontend
  pushd frontend
  call npm ci
  popd
)

REM Clear stale Vite cache (old 8080 gateway messages)
if exist "frontend\node_modules\.vite" rd /s /q "frontend\node_modules\.vite"

REM Load .env into this shell so Spring Boot sees DEEPGRAM_API_KEY, OPENROUTER_API_KEY, etc.
for /f "usebackq tokens=1,* delims==" %%A in (`findstr /R "^[A-Z0-9_][A-Z0-9_]*=.*" ".env"`) do (
  if not "%%A"=="" set "%%A=%%B"
)

if "%DB_PORT%"=="" set DB_PORT=5433
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=dev
set SERVER_PORT=8082
if "%JWT_SECRET%"=="" set "JWT_SECRET=local-dev-only-jwt-secret-do-not-use-in-prod-404E635266556A586E3272357538782F"
if "%GOOGLE_CLIENT_ID%"=="" set GOOGLE_CLIENT_ID=placeholder
if "%GOOGLE_CLIENT_SECRET%"=="" set GOOGLE_CLIENT_SECRET=placeholder
if "%GITHUB_CLIENT_ID%"=="" set GITHUB_CLIENT_ID=placeholder
if "%GITHUB_CLIENT_SECRET%"=="" set GITHUB_CLIENT_SECRET=placeholder

echo [..] Starting BACKEND API on http://localhost:8082
start "InterviAI-Backend" cmd /k "cd /d "%cd%\backend" && mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.port=8082""

timeout /t 12 /nobreak >nul

echo [..] Starting FRONTEND on http://localhost:5173 (proxies /api to 8082)
start "InterviAI-Frontend" cmd /k "cd /d "%cd%\frontend" && npm run dev -- --host 127.0.0.1 --port 5173 --force"

echo.
echo Open:  http://localhost:5173
echo API:   http://localhost:8082/actuator/health
echo.
echo Leave the two new CMD windows open.
endlocal
