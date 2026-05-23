# PostgreSQL Replication / Failover Demo - S32

## Objetivo

S32 agrega un demo aislado de alta disponibilidad de base de datos para CampusEnroll HA usando PostgreSQL streaming replication.

Lo que si demuestra:

- un PostgreSQL primary
- una PostgreSQL read replica
- streaming replication
- verificacion de lectura desde la replica
- failover manual promoviendo la replica
- switchover manual documentado como procedimiento operativo

Lo que no demuestra:

- reemplazo del `campusenroll-postgres` principal usado por la aplicacion
- failover automatico
- Patroni
- repmgr
- pg_auto_failover
- Kubernetes
- un cluster productivo multinodo

## Topologia

Archivo principal:

- `docker-compose.db-ha-demo.yml`

Contenedores:

- `campusenroll-pg-primary`
- `campusenroll-pg-replica`

Puertos host:

- primary: `56432 -> 5432`
- replica: `56433 -> 5432`

Volumenes:

- `campusenroll_pg_primary_data`
- `campusenroll_pg_replica_data`

Base demo:

- `campusenroll_ha_demo`

Tabla de evidencia:

- `replication_probe`

## Boundary importante

El stack principal de CampusEnroll sigue usando el puerto definido en `.env`, actualmente `55432`, para `campusenroll-postgres`.

Por eso:

- este demo se inicia por separado
- los microservicios no se apuntan a esta replica ni al primario del demo
- el demo S32 usa `56432` y `56433`, por lo que no necesita reemplazar ni apagar el PostgreSQL principal solo por puertos

Mensaje exacto para defensa:

`S32 implementa un demo aislado de replicacion streaming de PostgreSQL con failover manual por promocion de replica. No reemplaza la base principal del stack, no repunta a los microservicios y no equivale a failover automatico de produccion.`

## Activos agregados

- `infra/postgres-ha/primary/init/01-configure-primary.sh`
- `infra/postgres-ha/primary/init/02-replication-probe.sql`
- `infra/postgres-ha/replica/entrypoint.sh`
- `infra/postgres-ha/start-postgres-ha-demo.ps1`
- `infra/postgres-ha/verify-replication.ps1`
- `infra/postgres-ha/failover-promote-replica.ps1`
- `infra/postgres-ha/reset-postgres-ha-demo.ps1`
- `infra/postgres-ha/README.md`

Compatibilidad de checkout:

- `.gitattributes` fija `*.sh` con `eol=lf`
- esto evita que `infra/postgres-ha/primary/init/01-configure-primary.sh` y `infra/postgres-ha/replica/entrypoint.sh` fallen por CRLF cuando Docker o Linux los ejecutan desde un checkout Windows

## Validacion exacta

### 1. Validar Compose

```powershell
docker compose -f docker-compose.db-ha-demo.yml config
```

### 2. Mantener clara la separacion con el stack principal

No hace falta detener `campusenroll-postgres` solo por puertos. El stack principal sigue en `55432` y el demo S32 usa `56432` o `56433`.

### 3. Iniciar el demo

```powershell
docker compose -f docker-compose.db-ha-demo.yml down -v --remove-orphans
docker compose -f docker-compose.db-ha-demo.yml up -d --build
Start-Sleep -Seconds 45
docker compose -f docker-compose.db-ha-demo.yml ps
```

### 4. Verificar que el primary es primary

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
```

Esperado:

- `false`

### 5. Verificar que la replica es replica

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
```

Esperado:

- `true`

### 6. Verificar streaming desde el primary

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT application_name, state, sync_state FROM pg_stat_replication;"
```

Esperado:

- una fila para `campusenroll-pg-replica`
- `state` normalmente en `streaming`

### 7. Verificar WAL receiver en la replica

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT status, conninfo FROM pg_stat_wal_receiver;"
```

Esperado:

- `status` en `streaming`

### 8. Verificar que `replication_probe` existe en el primary

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT COUNT(*) FROM replication_probe;"
```

Esperado:

- la consulta funciona sin error

### 9. Insertar una fila en el primary

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('replicated-from-primary');"
```

### 10. Leer la fila desde la replica

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
```

Esperado:

- aparece la fila `replicated-from-primary`

### 11. Demostrar que la replica es read-only antes de promocion

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('should-fail-on-replica');"
```

Esperado:

- error de solo lectura porque la replica sigue en recovery

### 12. Failover manual

```powershell
docker stop campusenroll-pg-primary
docker exec -u postgres campusenroll-pg-replica pg_ctl -D /var/lib/postgresql/data promote
```

### 13. Verificar promocion de la replica

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
```

Esperado:

- `false`

### 14. Verificar escrituras sobre la replica promovida

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('written-after-promotion'); SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
```

Esperado:

- la insercion funciona

### 15. Limpieza

```powershell
docker compose -f docker-compose.db-ha-demo.yml down -v --remove-orphans
```

## Switchover manual documentado

Este repositorio no implementa switchover automatico. El flujo correcto para un switchover manual de demo es:

1. pausar escrituras conceptualmente sobre el primary actual
2. verificar que la replica sigue en `streaming`
3. detener el primary
4. promover la replica con `pg_ctl ... promote`
5. mover manualmente el cliente de `localhost:56432` a `localhost:56433`
6. verificar que la replica promovida devuelve `pg_is_in_recovery() = false`
7. validar una escritura sobre la replica promovida

Mensaje exacto:

`Esto es un switchover manual de demo. No hay eleccion automatica de lider, no hay fencing y el cliente se mueve manualmente.`

## Scripts PowerShell utiles

Inicio:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/start-postgres-ha-demo.ps1
```

Verificacion:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/verify-replication.ps1 -InsertProbe
```

Failover por promocion:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/failover-promote-replica.ps1
```

Reset:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/reset-postgres-ha-demo.ps1
```

## Limites frente a produccion

- no hay failover automatico
- no hay leader election
- no hay fencing
- no hay client rerouting automatico
- no hay health-based promotion automatica
- no hay integracion de microservicios contra la replica
- no hay cluster Patroni, repmgr ni pg_auto_failover

Para HA real de base de datos todavia harian falta:

- automatizacion de promocion
- monitoreo y alertas especificas de replica
- politica de reroute de clientes
- procedimientos ensayados de failover y failback
- proteccion contra split-brain
- estrategia de backups y restauracion coordinada con la topologia replicada
