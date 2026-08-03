# Start InterviAI backend on Windows PowerShell.
# Usage (from repo root OR backend folder):
#   .\scripts\run-backend.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path "$Root\backend\pom.xml")) {
  # Allow running while already inside backend/
  if (Test-Path ".\pom.xml") { $Root = Resolve-Path ".." } else { throw "Run from interviewAi repo" }
}

Set-Location "$Root\backend"

if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = "8082" }
if (-not $env:DB_HOST) { $env:DB_HOST = "localhost" }
if (-not $env:DB_PORT) { $env:DB_PORT = "5433" }
if (-not $env:JWT_SECRET) {
  $env:JWT_SECRET = "local-dev-only-jwt-secret-do-not-use-in-prod-404E635266556A586E3272357538782F"
}
if (-not $env:GOOGLE_CLIENT_ID) { $env:GOOGLE_CLIENT_ID = "placeholder-google-client-id" }
if (-not $env:GOOGLE_CLIENT_SECRET) { $env:GOOGLE_CLIENT_SECRET = "placeholder-google-client-secret" }
if (-not $env:GITHUB_CLIENT_ID) { $env:GITHUB_CLIENT_ID = "placeholder-github-client-id" }
if (-not $env:GITHUB_CLIENT_SECRET) { $env:GITHUB_CLIENT_SECRET = "placeholder-github-client-secret" }

Write-Host "Starting backend profile=$($env:SPRING_PROFILES_ACTIVE) port=$($env:SERVER_PORT) db=$($env:DB_HOST):$($env:DB_PORT)"

# Quotes are REQUIRED in PowerShell so -D... is not split into a fake Maven phase.
mvn spring-boot:run "-Dspring-boot.run.profiles=$($env:SPRING_PROFILES_ACTIVE)" "-Dspring-boot.run.arguments=--server.port=$($env:SERVER_PORT)"
