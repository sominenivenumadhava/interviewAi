@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
echo Starting FRONTEND only on http://localhost:5173
where npm >nul 2>&1 || (echo [ERROR] npm required & exit /b 1)
cd frontend
if not exist node_modules call npm ci
npm run dev -- --host 127.0.0.1 --port 5173
endlocal
