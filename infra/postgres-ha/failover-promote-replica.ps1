[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$database = 'campusenroll_ha_demo'

docker stop campusenroll-pg-primary | Out-Null
docker exec -u postgres campusenroll-pg-replica pg_ctl -D /var/lib/postgresql/data promote | Out-Null
Start-Sleep -Seconds 5
docker exec -i campusenroll-pg-replica psql -U campus -d $database -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-replica psql -U campus -d $database -c "INSERT INTO replication_probe(label) VALUES ('written-after-promotion'); SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
Write-Host "Manual client target should now move from localhost:56432 to localhost:56433."
