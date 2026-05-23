[CmdletBinding()]
param(
    [switch]$InsertProbe,
    [string]$ProbeLabel = 'replicated-from-primary'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$database = 'campusenroll_ha_demo'
$primary = 'campusenroll-pg-primary'
$replica = 'campusenroll-pg-replica'

docker exec -i $primary psql -U campus -d $database -c "SELECT pg_is_in_recovery();"
docker exec -i $replica psql -U campus -d $database -c "SELECT pg_is_in_recovery();"
docker exec -i $primary psql -U campus -d $database -c "SELECT application_name, state, sync_state FROM pg_stat_replication;"
docker exec -i $replica psql -U campus -d $database -c "SELECT status, conninfo FROM pg_stat_wal_receiver;"

if ($InsertProbe) {
    docker exec -i $primary psql -U campus -d $database -c "INSERT INTO replication_probe(label) VALUES ('$ProbeLabel');"
}

docker exec -i $replica psql -U campus -d $database -c "SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
