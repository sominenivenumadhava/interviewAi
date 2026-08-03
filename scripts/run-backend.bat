@echo off
setlocal EnableExtensions
cd /d "%~dp0.."

echo Starting BACKEND only on http://localhost:8082
where java >nul 2>&1 || (echo [ERROR] Java 21+ required & exit /b 1)
where mvn  >nul 2>&1 || (echo [ERROR] Maven required & exit /b 1)

if not exist ".env" copy /Y ".env.template" ".env" >nul

REM Load common env vars for this shell (best-effort)
for /f "usebackq tokens=1,* delims==" %%A in (`findstr /R "^[A-Z0-9_][A-Z0-9_]*=.*" ".env"`) do (
  if not "%%A"=="" set "%%A=%%B"
)

if "%DB_PORT%"=="" set DB_PORT=5433
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=dev
if "%SERVER_PORT%"=="" set SERVER_PORT=8082
if "%JWT_SECRET%"=="" set "JWT_SECRET=local-dev-only-jwt-secret-do-not-use-in-prod-404E635266556A586E3272357538782F"
if "%GOOGLE_CLIENT_ID%"=="" set GOOGLE_CLIENT_ID=placeholder-google-client-id
if "%GOOGLE_CLIENT_SECRET%"=="" set GOOGLE_CLIENT_SECRET=placeholder-google-client-secret
if "%GITHUB_CLIENT_ID%"=="" set GITHUB_CLIENT_ID=placeholder-github-client-id
if "%GITHUB_CLIENT_SECRET%"=="" set GITHUB_CLIENT_SECRET=placeholder-github-client-secret

cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--server.port=%SERVER_PORT%
endlocal
