# InterviAI — Windows local fix & run (PowerShell)
# Usage:
#   cd C:\Users\venum\Desktop\dumm\interviewAi
#   powershell -ExecutionPolicy Bypass -File .\scripts\fix-and-run-local.ps1
#
# Architecture: ONE API = Spring Boot on port 8082
#               Frontend Vite on 5173 proxies /api -> 8082
#               No separate API gateway on 8080.

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $Root "backend"))) {
  $Root = Get-Location
}
Set-Location $Root

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " InterviAI local fix (API = port 8082 only)" -ForegroundColor Cyan
Write-Host " Root: $Root" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

function Assert-Cmd($name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    throw "Missing dependency: $name"
  }
}

Assert-Cmd java
Assert-Cmd mvn
Assert-Cmd node
Assert-Cmd npm

function Stop-Port($port) {
  try {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
      $procId = $c.OwningProcess
      if ($procId -and $procId -ne 0) {
        Write-Host "[..] Killing PID $procId on port $port" -ForegroundColor Yellow
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
      }
    }
  } catch {}
}

# 1) Sync to latest branch (force clean frontend messaging about 8080 gateway)
Write-Host "[1/7] Syncing git branch..." -ForegroundColor Green
git fetch origin 2>$null
git checkout redesign/modern-ai-ui-and-bugfixes 2>$null
git pull origin redesign/modern-ai-ui-and-bugfixes

# 2) .env — single API port 8082
Write-Host "[2/7] Configuring .env for port 8082..." -ForegroundColor Green
if (-not (Test-Path ".env")) {
  Copy-Item ".env.template" ".env"
}
$envText = Get-Content ".env" -Raw
$envText = $envText -replace '(?m)^SERVER_PORT=.*', 'SERVER_PORT=8082'
$envText = $envText -replace '(?m)^VITE_API_BASE_URL=.*', 'VITE_API_BASE_URL=http://localhost:8082'
if ($envText -notmatch '(?m)^SERVER_PORT=') { $envText += "`nSERVER_PORT=8082`n" }
if ($envText -notmatch '(?m)^VITE_API_BASE_URL=') { $envText += "`nVITE_API_BASE_URL=http://localhost:8082`n" }
# Prefer Docker Postgres mapping
if (Get-Command docker -ErrorAction SilentlyContinue) {
  $envText = $envText -replace '(?m)^DB_PORT=.*', 'DB_PORT=5433'
  $envText = $envText -replace '(?m)^DB_PASSWORD=.*', 'DB_PASSWORD=interviai_password'
}
Set-Content ".env" $envText -NoNewline

# 3) Free ports
Write-Host "[3/7] Freeing ports 8080 / 8082 / 5173..." -ForegroundColor Green
Stop-Port 8080
Stop-Port 8082
Stop-Port 5173
Start-Sleep -Seconds 2

# 4) Postgres
Write-Host "[4/7] Starting Postgres + Redis..." -ForegroundColor Green
if (Get-Command docker -ErrorAction SilentlyContinue) {
  docker compose -f docker-compose.dev.yml up -d postgres redis
} else {
  Write-Host "[WARN] Docker not found — ensure Postgres is running (DB_PORT in .env)" -ForegroundColor Yellow
}

# 5) Load .env into process for child windows
Write-Host "[5/7] Loading environment..." -ForegroundColor Green
Get-Content ".env" | ForEach-Object {
  if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
  $pair = $_.Split('=', 2)
  if ($pair.Length -eq 2) {
    $k = $pair[0].Trim()
    $v = $pair[1].Trim()
    [Environment]::SetEnvironmentVariable($k, $v, "Process")
    Set-Item -Path "Env:$k" -Value $v
  }
}
if (-not $env:GOOGLE_CLIENT_ID) { $env:GOOGLE_CLIENT_ID = "placeholder" }
if (-not $env:GOOGLE_CLIENT_SECRET) { $env:GOOGLE_CLIENT_SECRET = "placeholder" }
if (-not $env:GITHUB_CLIENT_ID) { $env:GITHUB_CLIENT_ID = "placeholder" }
if (-not $env:GITHUB_CLIENT_SECRET) { $env:GITHUB_CLIENT_SECRET = "placeholder" }
if (-not $env:JWT_SECRET) {
  $env:JWT_SECRET = "local-dev-only-jwt-secret-do-not-use-in-prod-404E635266556A586E3272357538782F"
}
$env:SERVER_PORT = "8082"
$env:SPRING_PROFILES_ACTIVE = "dev"

# 6) Frontend deps
Write-Host "[6/7] Ensuring frontend deps..." -ForegroundColor Green
Push-Location frontend
if (-not (Test-Path "node_modules")) { npm ci }
# Clear Vite cache so old "8080 gateway" bundle cannot stick
if (Test-Path "node_modules\.vite") { Remove-Item -Recurse -Force "node_modules\.vite" }
Pop-Location

# 7) Start backend + frontend in new windows
Write-Host "[7/7] Starting backend (8082) and frontend (5173)..." -ForegroundColor Green

$beCmd = @"
cd /d "$Root\backend"
set SERVER_PORT=8082
set SPRING_PROFILES_ACTIVE=dev
set GOOGLE_CLIENT_ID=%GOOGLE_CLIENT_ID%
set GOOGLE_CLIENT_SECRET=%GOOGLE_CLIENT_SECRET%
set GITHUB_CLIENT_ID=%GITHUB_CLIENT_ID%
set GITHUB_CLIENT_SECRET=%GITHUB_CLIENT_SECRET%
set JWT_SECRET=%JWT_SECRET%
set DB_HOST=%DB_HOST%
set DB_PORT=%DB_PORT%
set DB_NAME=%DB_NAME%
set DB_USERNAME=%DB_USERNAME%
set DB_PASSWORD=%DB_PASSWORD%
set OPENROUTER_API_KEY=%OPENROUTER_API_KEY%
set DEEPGRAM_API_KEY=%DEEPGRAM_API_KEY%
echo Starting Spring Boot API on port 8082...
mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.port=8082"
"@

$feCmd = @"
cd /d "$Root\frontend"
echo Starting Vite on port 5173 (proxies /api -> 8082)...
npm run dev -- --host 127.0.0.1 --port 5173 --force
"@

Start-Process cmd.exe -ArgumentList "/k", $beCmd
Start-Sleep -Seconds 3
Start-Process cmd.exe -ArgumentList "/k", $feCmd

Write-Host ""
Write-Host "Waiting for backend health on http://localhost:8082/actuator/health ..." -ForegroundColor Cyan
$ok = $false
for ($i = 1; $i -le 60; $i++) {
  try {
    $r = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -UseBasicParsing -TimeoutSec 2
    if ($r.StatusCode -eq 200) { $ok = $true; break }
  } catch {}
  Start-Sleep -Seconds 3
  Write-Host "  ... still starting ($i/60)"
}

if ($ok) {
  Write-Host "Backend is UP on port 8082" -ForegroundColor Green
  Start-Process "http://localhost:5173/register"
} else {
  Write-Host "Backend did not become healthy in time. Check the InterviAI backend CMD window for errors." -ForegroundColor Red
  Write-Host "Common fixes: free port 8082, start Docker Postgres, check DB_PORT=5433 in .env" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "UI:  http://localhost:5173" -ForegroundColor Green
Write-Host "API: http://localhost:8082  (single backend port — no gateway on 8080)" -ForegroundColor Green
