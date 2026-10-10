$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root 'backend'

Write-Host 'Starting Shadow Engineer API on http://localhost:8080...' -ForegroundColor Cyan
$apiCommand = "Set-Location '$backend'; Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue; mvn spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit', '-Command', $apiCommand

Write-Host 'Starting Shadow Engineer frontend on http://localhost:5173...' -ForegroundColor Cyan
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root'; npm run dev"

Write-Host 'Both processes started. Open http://localhost:5173 when the API is ready.' -ForegroundColor Green
