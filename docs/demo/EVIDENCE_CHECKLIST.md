# Evidence Checklist - Entrega Final S20

## Instrucciones

Guardar capturas o salidas con nombres estables. Cuando sea posible, incluir el nombre del archivo, el endpoint, el comando o la URL visible.

## 1. Repositorio y contexto

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `01_repo_root.png` | Raiz del repo con `backend/`, `db/`, `docs/`, `infra/`, `postman/` y los dos Compose files. |
| [ ] | `02_readme_s20.png` | `README.md` actualizado con standard mode y HA readiness mode. |
| [ ] | `03_ha_compose_file.png` | Fragmento de `docker-compose.apps.yml` con servicios, restart policy y healthchecks. |

## 2. Standard mode: infraestructura compartida

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `04_standard_compose_ps.png` | `docker compose ps` con PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana arriba. |
| [ ] | `05_infra_healthchecks.png` | Fragmento de `docker-compose.yml` con healthchecks de PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana. |
| [ ] | `06_env_ports.png` | `.env` mostrando `POSTGRES_PORT` y puertos de infraestructura. |

## 3. Base de datos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `07_schema_loaded.png` | Ejecucion de `db/schema.sql` sin error. |
| [ ] | `08_seed_loaded.png` | Ejecucion de `db/data.sql` sin error. |
| [ ] | `09_db_tables.png` | `\dt` o lista de tablas principales. |
| [ ] | `10_db_seed_query.png` | Una consulta a `students`, `sections`, `enrollments` o `billings`. |

## 4. HA readiness mode: aplicaciones contenedorizadas

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `11_apps_compose_up.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build`. |
| [ ] | `12_apps_compose_ps.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps` con los cinco servicios y la infraestructura. |
| [ ] | `13_apps_healthy.png` | Estado `healthy` para contenedores que ya completaron healthcheck. |

## 5. Health checks HTTP

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `14_health_checks.png` | Las cinco respuestas `GET /health` con `status=UP`. |

## 6. Reinicio y recuperacion de servicio

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `15_course_stopped.png` | `docker compose ... stop course-service` y el estado detenido. |
| [ ] | `16_course_started.png` | `docker compose ... start course-service` y el estado recuperado. |
| [ ] | `17_course_health_recovered.png` | `curl.exe http://localhost:8082/health` respondiendo otra vez. |

## 7. Postman

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `18_postman_collection.png` | Coleccion importada. |
| [ ] | `19_postman_environment.png` | Environment local seleccionado. |
| [ ] | `20_students_folder.png` | Carpeta `01 - Students` o una respuesta exitosa de estudiantes. |
| [ ] | `21_catalog_folder.png` | Carpeta `02 - Academic Catalog` o una respuesta exitosa del catalogo. |
| [ ] | `22_enrollment_created.png` | `POST /api/enrollments` exitoso. |
| [ ] | `23_enrollment_conflict_409.png` | Reintento del mismo `POST /api/enrollments` devolviendo `409`. |
| [ ] | `24_billing_created.png` | `POST /api/billings` exitoso. |
| [ ] | `25_billing_conflict_409.png` | Reintento del mismo `POST /api/billings` devolviendo `409`. |
| [ ] | `26_notification_health.png` | `GET /health` de `notification`. |

## 8. Redis cache

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `27_redis_ping.png` | `redis-cli ping` con `PONG`. |
| [ ] | `28_courses_cache_keys.png` | `GET /api/courses` repetido y `redis-cli --scan --pattern "courses::*"`. |
| [ ] | `29_cache_fallback_warning.png` | Fallback de cache cuando Redis se detiene. |

## 9. RabbitMQ y eventos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `30_rabbitmq_ui.png` | RabbitMQ Management UI en `http://localhost:15672`. |
| [ ] | `31_rabbitmq_bindings.png` | Exchange `campusenroll.events`, queue `notification.events` y bindings. |
| [ ] | `32_enrollment_event_log.png` | `Published EnrollmentCreatedEvent ...` en `enrollment-service`. |
| [ ] | `33_billing_event_log.png` | `Published BillingStatusChangedEvent ...` en `billing-service`. |
| [ ] | `34_notification_event_logs.png` | Logs de `notification` con ambos eventos recibidos. |

## 10. k6

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `35_k6_smoke_summary.png` | Resumen de `smoke-test.js`. |
| [ ] | `36_k6_50000_summary.png` | Resumen de `load-50000-requests.js`. |
| [ ] | `37_k6_concurrent_summary.png` | Resumen de `concurrent-enrollment-test.js`. |
| [ ] | `38_k6_metrics_focus.png` | Un recorte donde se vean `total requests`, `http_req_failed`, `checks`, p95, p99 y throughput. |

## 11. Observabilidad

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `39_prometheus_targets.png` | `http://localhost:9090/targets` con `prometheus` en `UP`. |
| [ ] | `40_grafana_access.png` | `http://localhost:3000` accesible. |

## 12. Falla controlada de infraestructura

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `41_redis_stopped.png` | `docker compose stop redis` y estado del contenedor detenido. |
| [ ] | `42_courses_with_redis_down.png` | `GET /api/courses` funcionando con Redis caido. |
| [ ] | `43_redis_recovered.png` | Redis reiniciado y llaves reapareciendo. |

## 13. Mensaje final honesto

Verificar que las capturas permitan sostener estas afirmaciones:

- `docker-compose.yml` sigue siendo standard mode para infraestructura compartida.
- `docker-compose.apps.yml` agrega un modo demostrable de HA readiness para los cinco servicios Spring Boot.
- Existen restart policies y healthchecks tanto en infraestructura como en servicios de aplicacion.
- Redis si esta integrado en `course-service`.
- RabbitMQ si esta integrado para publicacion y consumo de eventos de evidencia.
- k6, Prometheus y Grafana existen como activos reales del repo.
- El estado actual es `HA-ready` para entrega local, no alta disponibilidad productiva.
- Siguen pendientes replicas reales, balanceador, cluster Redis, cluster RabbitMQ y failover PostgreSQL.
