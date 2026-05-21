# Evidence Checklist - Entrega Final S22

## Instrucciones

Guardar capturas o salidas con nombres estables. Cuando sea posible, incluir el nombre del archivo, el endpoint, el comando o la URL visible.

## 1. Repositorio y contexto

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `01_repo_root.png` | Raiz del repo con `backend/`, `db/`, `docs/`, `infra/`, `postman/` y los dos Compose files. |
| [ ] | `02_readme_s22.png` | `README.md` actualizado con standard mode, HA readiness mode, metricas Prometheus y backup/restore PostgreSQL. |
| [ ] | `03_ha_compose_file.png` | Fragmento de `docker-compose.apps.yml` con servicios, restart policy y healthchecks. |

## 2. Standard mode e infraestructura compartida

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `04_standard_compose_ps.png` | `docker compose ps` con PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana arriba. |
| [ ] | `05_infra_healthchecks.png` | Fragmento de `docker-compose.yml` con healthchecks de PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana. |
| [ ] | `06_env_ports.png` | `.env` mostrando `POSTGRES_PORT` y puertos de infraestructura. |

## 3. Base de datos y backup

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `07_schema_loaded.png` | Ejecucion de `db/schema.sql` sin error. |
| [ ] | `08_seed_loaded.png` | Ejecucion de `db/data.sql` sin error. |
| [ ] | `09_db_tables.png` | `\dt` o lista de tablas principales. |
| [ ] | `10_db_seed_query.png` | Una consulta a `students`, `sections`, `enrollments` o `billings`. |
| [ ] | `11_db_verify_script.png` | `powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1` con base, usuario y conteos. |
| [ ] | `12_db_backup_created.png` | `backup-postgres.ps1` creando el archivo `.dump` con exito. |
| [ ] | `13_db_backup_output.png` | `Get-ChildItem infra/backups/output` mostrando el dump generado. |

## 4. HA readiness mode: aplicaciones contenedorizadas

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `14_apps_compose_up.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build`. |
| [ ] | `15_apps_compose_ps.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps` con los cinco servicios y la infraestructura. |
| [ ] | `16_apps_healthy.png` | Estado `healthy` para contenedores que ya completaron healthcheck. |

## 5. Health checks HTTP

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `17_health_checks.png` | Las cinco respuestas `GET /health` con `status=UP`. |

## 6. Reinicio y recuperacion de servicio

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `18_course_stopped.png` | `docker compose ... stop course-service` y el estado detenido. |
| [ ] | `19_course_started.png` | `docker compose ... start course-service` y el estado recuperado. |
| [ ] | `20_course_health_recovered.png` | `curl.exe http://localhost:8082/health` respondiendo otra vez. |

## 7. Postman

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `21_postman_collection.png` | Coleccion importada. |
| [ ] | `22_postman_environment.png` | Environment local seleccionado. |
| [ ] | `23_students_folder.png` | Carpeta `01 - Students` o una respuesta exitosa de estudiantes. |
| [ ] | `24_catalog_folder.png` | Carpeta `02 - Academic Catalog` o una respuesta exitosa del catalogo. |
| [ ] | `25_enrollment_created.png` | `POST /api/enrollments` exitoso. |
| [ ] | `26_enrollment_conflict_409.png` | Reintento del mismo `POST /api/enrollments` devolviendo `409`. |
| [ ] | `27_billing_created.png` | `POST /api/billings` exitoso. |
| [ ] | `28_billing_conflict_409.png` | Reintento del mismo `POST /api/billings` devolviendo `409`. |
| [ ] | `29_notification_health.png` | `GET /health` de `notification`. |

## 8. Redis cache

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `30_redis_ping.png` | `redis-cli ping` con `PONG`. |
| [ ] | `31_courses_cache_keys.png` | `GET /api/courses` repetido y `redis-cli --scan --pattern "courses::*"`. |
| [ ] | `32_cache_fallback_warning.png` | Fallback de cache cuando Redis se detiene. |

## 9. RabbitMQ y eventos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `33_rabbitmq_ui.png` | RabbitMQ Management UI en `http://localhost:15672`. |
| [ ] | `34_rabbitmq_bindings.png` | Exchange `campusenroll.events`, queue `notification.events` y bindings. |
| [ ] | `35_enrollment_event_log.png` | `Published EnrollmentCreatedEvent ...` en `enrollment-service`. |
| [ ] | `36_billing_event_log.png` | `Published BillingStatusChangedEvent ...` en `billing-service`. |
| [ ] | `37_notification_event_logs.png` | Logs de `notification` con ambos eventos recibidos. |

## 10. k6

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `38_k6_smoke_summary.png` | Resumen de `smoke-test.js`. |
| [ ] | `39_k6_50000_summary.png` | Resumen de `load-50000-requests.js`. |
| [ ] | `40_k6_concurrent_summary.png` | Resumen de `concurrent-enrollment-test.js`. |
| [ ] | `41_k6_metrics_focus.png` | Un recorte donde se vean `total requests`, `http_req_failed`, `checks`, p95, p99 y throughput. |

## 11. Observabilidad

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `42_actuator_prometheus.png` | Las cinco respuestas `GET /actuator/prometheus` con metricas no vacias. |
| [ ] | `43_prometheus_targets_apps.png` | `http://localhost:9090/targets` con `prometheus`, `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` en `UP`. |
| [ ] | `44_grafana_access.png` | `http://localhost:3000` accesible. |

## 12. Falla controlada de infraestructura

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `45_redis_stopped.png` | `docker compose stop redis` y estado del contenedor detenido. |
| [ ] | `46_courses_with_redis_down.png` | `GET /api/courses` funcionando con Redis caido. |
| [ ] | `47_redis_recovered.png` | Redis reiniciado y llaves reapareciendo. |

## 13. Backup, restore y recuperacion de base de datos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `48_restore_warning.png` | `restore-postgres.ps1` mostrando la advertencia de sobrescritura y el uso de `-Force`. |
| [ ] | `49_restore_success.png` | `restore-postgres.ps1` completando `pg_restore` sin error. |
| [ ] | `50_verify_after_restore.png` | `verify-database.ps1` despues del restore. |
| [ ] | `51_runbook_open.png` | `infra/backups/DISASTER_RECOVERY_RUNBOOK.md` abierto con escenarios y comandos. |

## 14. Mensaje final honesto

Verificar que las capturas permitan sostener estas afirmaciones:

- `docker-compose.yml` sigue siendo standard mode para infraestructura compartida.
- `docker-compose.apps.yml` agrega un modo demostrable de HA readiness para los cinco servicios Spring Boot.
- Existen restart policies y healthchecks tanto en infraestructura como en servicios de aplicacion.
- S21 agrego metricas Prometheus reales en los cinco microservicios.
- S22 agrega backup manual, restore manual y runbook de recuperacion para PostgreSQL.
- Los targets de Prometheus muestran a las cinco apps en `UP` cuando el modo HA readiness esta activo.
- Redis si esta integrado en `course-service`.
- RabbitMQ si esta integrado para publicacion y consumo de eventos de evidencia.
- Grafana sigue accesible, pero dashboards y alertas de negocio siguen pendientes.
- k6, Prometheus y Grafana existen como activos reales del repo.
- El estado actual es `HA-ready` para entrega local, no alta disponibilidad productiva.
- Siguen pendientes replicas reales, balanceador, cluster Redis, cluster RabbitMQ, failover PostgreSQL, backups programados, almacenamiento off-site y cifrado.
