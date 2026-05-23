[CmdletBinding()]
param(
    [switch]$ForceStopMainPostgres
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$composeArgs = @('-f', 'docker-compose.db-ha-demo.yml')
$mainPostgresRunning = docker ps --format '{{.Names}}' | Select-String -SimpleMatch 'campusenroll-postgres'

if ($mainPostgresRunning) {
    if (-not $ForceStopMainPostgres) {
        throw "campusenroll-postgres is already running on host port 56432. Stop that container first or rerun with -ForceStopMainPostgres."
    }

    docker stop campusenroll-postgres | Out-Null
}

docker compose @composeArgs up -d --build
docker compose @composeArgs ps
