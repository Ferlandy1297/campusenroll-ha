# CampusEnroll HA

## Estado actual

CampusEnroll HA ya tiene una base funcional para:

- `student-service` con endpoints de estudiantes
- `course-service` con catalogo academico y cache Redis
- `enrollment-service` con inscripciones y outbox transaccional para `EnrollmentCreatedEvent`
- `billing-service` con cobros y outbox transaccional para `BillingStatusChangedEvent`
- `Idempotency-Key` real en `POST /api/enrollments` y `POST /api/billings`
- `notification` como consumidor RabbitMQ para evidencia y logs
- metricas reales Actuator/Prometheus en los cinco microservicios
- `db/schema.sql` y `db/data.sql` para carga determinista de PostgreSQL
- `infra/backups/` con scripts PowerShell para backup, restore y verificacion de PostgreSQL
- `docker-compose.ha-demo.yml` e `infra/load-balancer/` para failover y switchover de aplicacion en `course-service`
- `postman/` como cliente operativo actual
- `infra/k6/` como paquete de validacion final

La entrega actual ya no depende solo del flujo local con Maven. El repo soporta dos modos de ejecucion sin reemplazar el workflow existente.

S22 agrega una capa practica de backup y recuperacion ante desastres para PostgreSQL. Esto fortalece la continuidad operativa local y la narrativa de HA readiness sin convertir el proyecto en una plataforma productiva de alta disponibilidad.

S25 agrega una capa de failover y switchover a nivel de aplicacion para `course-service` mediante HAProxy y `course-service-replica`. Esto protege el catalogo academico ante la caida del proceso del servicio, pero no cambia la regla de PostgreSQL centralizado ni implementa failover de base de datos.

Alcance honesto:

- backend, base de datos, microservicios, observabilidad y recuperacion
- sin frontend; Postman sigue siendo el cliente operativo actual

## Modos de ejecucion

### 1. Standard mode

`docker-compose.yml` sigue levantando solo infraestructura compartida:

- PostgreSQL
- Redis
- RabbitMQ
- Prometheus
- Grafana

Comando base:

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

Este modo mantiene intacto el flujo local actual con `mvn spring-boot:run`.

### 2. HA readiness mode

`docker-compose.apps.yml` agrega los cinco servicios Spring Boot como contenedores:

- `student-service` en `8081`
- `course-service` en `8082`
- `enrollment-service` en `8083`
- `billing-service` en `8084`
- `notification` en `8085`

Comando de arranque:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
```

Importante:

- este modo es `HA-ready` y demostrable para la entrega, no alta disponibilidad productiva
- la primera ejecucion sobre un volumen PostgreSQL nuevo todavia requiere cargar `db/schema.sql` y `db/data.sql`
- el workflow con Maven local sigue siendo valido y no fue removido
- Prometheus scrapea `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` por nombre interno Docker cuando este modo esta activo
- si Prometheus ya estaba corriendo antes del cambio de configuracion, reinicialo una vez con `docker compose -f docker-compose.yml -f docker-compose.apps.yml restart prometheus`

### 3. Application failover / switchover demo

`docker-compose.ha-demo.yml` agrega una tercera capa de demo para continuidad del catalogo:

- `course-service-replica` sin publicar `8082` al host
- `haproxy` en `http://localhost:8080`
- stats locales de HAProxy en `http://localhost:8404/stats`

Comandos:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

Esta capa demuestra:

- failover y switchover a nivel de aplicacion para `GET /api/courses`, `GET /api/periods`, `GET /api/sections` y `GET /health/course`
- chequeo de salud HTTP con `GET /health`
- continuidad del gateway cuando el `course-service` primario se detiene

Importante:

- esto no implementa failover de PostgreSQL
- recuperacion de base de datos sigue en `infra/backups/`
- guia detallada: [Application Failover / Switchover Demo](docs/ha/APPLICATION_FAILOVER_SWITCHOVER_DEMO.md)

## Flujo local recomendado

1. Confirmar `.env`.
2. Levantar infraestructura compartida o el stack completo.
3. Cargar base de datos demo si el volumen es nuevo.
4. Elegir uno de estos caminos:
   - ejecutar servicios localmente con `mvn spring-boot:run`
   - ejecutar servicios con `docker-compose.apps.yml`
5. Verificar salud con `GET /health` y endpoints Actuator.
6. Verificar Prometheus en `http://localhost:9090/targets` si el modo HA readiness esta activo.
7. Ejecutar Postman, Redis, RabbitMQ, k6 y la evidencia final.
8. Ejecutar backup/restore de PostgreSQL cuando se necesite preservar o recuperar el dataset local.

Carga de base de datos:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Health checks:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Actuator metrics:

```powershell
curl.exe http://localhost:8081/actuator/prometheus
curl.exe http://localhost:8082/actuator/prometheus
curl.exe http://localhost:8083/actuator/prometheus
curl.exe http://localhost:8084/actuator/prometheus
curl.exe http://localhost:8085/actuator/prometheus
```

## Backup y recuperacion S22

Comandos base:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
Get-ChildItem infra/backups/output
powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

Que hace esta capa:

- genera dumps PostgreSQL en formato custom con `pg_dump -Fc`
- restaura con `pg_restore --clean --if-exists`
- deja un runbook local en `infra/backups/DISASTER_RECOVERY_RUNBOOK.md`

Mensaje honesto:

- esto ya permite defender backup y recuperacion local de la base de datos
- no equivale todavia a backups programados, almacenamiento off-site, cifrado, ni retencion productiva

## Que esta implementado

- `restart: unless-stopped` en infraestructura y servicios de aplicacion en modo Compose
- healthchecks para PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana y los cinco servicios Spring Boot
- contenedorizacion de los cinco servicios de negocio mediante `docker-compose.apps.yml`
- `docker-compose.ha-demo.yml` con `course-service-replica` y HAProxy para continuidad del catalogo academico
- endpoints `GET /actuator/health`, `GET /actuator/info` y `GET /actuator/prometheus` en los cinco servicios
- Prometheus scrapeando metricas reales de los cinco microservicios en modo HA readiness
- reglas activas de alerta Prometheus para caida de servicio, target faltante, error HTTP y p95 de latencia
- Redis real en `course-service`
- outbox transaccional real en `enrollment-service` y `billing-service` para persistir eventos antes de publicarlos
- RabbitMQ real para publicacion asincrona y consumo de eventos de evidencia
- failover y switchover a nivel de aplicacion para `course-service` usando health checks HTTP en HAProxy
- backup manual de PostgreSQL con `infra/backups/backup-postgres.ps1`
- restore manual de PostgreSQL con `infra/backups/restore-postgres.ps1`
- verificacion de base con `infra/backups/verify-database.ps1`
- runbook local de recuperacion ante desastres en `infra/backups/DISASTER_RECOVERY_RUNBOOK.md`
- k6 como paquete de validacion final
- Grafana accesible como infraestructura disponible
- Alertmanager y notificaciones externas siguen fuera del alcance actual

## Que sigue siendo mejora futura

- cluster multinodo real
- failover equivalente para `student-service`, `enrollment-service`, `billing-service` y `notification`
- replicacion o failover de PostgreSQL
- Redis cluster
- RabbitMQ cluster
- entrypoint unico para todos los microservicios detras del balanceador
- Kubernetes o Docker Swarm
- dashboards Grafana listos para plataforma y negocio
- Alertmanager, enrutamiento de notificaciones y observabilidad operativa de produccion
- compensacion completa de sagas
- backups programados
- almacenamiento off-site
- cifrado de backups
- retencion automatizada y pruebas periodicas de restauracion

## URLs utiles

- RabbitMQ Management UI: `http://localhost:15672`
- Prometheus: `http://localhost:9090`
- Prometheus targets: `http://localhost:9090/targets`
- Grafana: `http://localhost:3000`
- HAProxy gateway: `http://localhost:8080`
- HAProxy stats: `http://localhost:8404/stats`

## Documentacion recomendada

- `docs/demo/DEMO_COMMANDS.md`
- `docs/demo/EVIDENCE_CHECKLIST.md`
- `docs/ha/APPLICATION_FAILOVER_SWITCHOVER_DEMO.md`
- `docs/final/CHECKPOINT_1_PDF_READY.md`
- `docs/final/EVIDENCE_PLACEHOLDERS.md`
- `infra/backups/DISASTER_RECOVERY_RUNBOOK.md`
- `infra/k6/README.md`

## Course Alignment Audit

- `docs/course-audit/README.md`
- `docs/course-audit/`
- `docs/course-audit/S27_EVIDENCE_SUMMARY.md`
- `docs/course-audit/SLO_ALERTING_PROPOSAL.md`
- `docs/course-audit/TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`
