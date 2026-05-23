[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

docker compose -f docker-compose.db-ha-demo.yml down -v
