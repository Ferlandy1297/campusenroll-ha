# Evidence Checklist - Entrega Final S19

## Instrucciones

Guardar capturas o salidas con nombres estables. Cuando sea posible, incluir el nombre del archivo, el endpoint, el comando o la URL visible.

## 1. Repositorio y contexto

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `01_repo_root.png` | Raiz del repo con `backend/`, `db/`, `docs/`, `infra/` y `postman/`. |
| [ ] | `02_readme_actual.png` | `README.md` actualizado con el estado actual del proyecto. |
| [ ] | `03_checkpoint_summary.png` | `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md` visible. |

## 2. Infraestructura compartida

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `04_compose_ps.png` | `docker compose ps` con PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana arriba. |
| [ ] | `05_compose_file.png` | Fragmento de `docker-compose.yml` con puertos y servicios de infraestructura. |
| [ ] | `06_env_ports.png` | `.env` mostrando el `POSTGRES_PORT` actual y puertos de infraestructura. |

## 3. Base de datos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `07_schema_loaded.png` | Ejecucion de `db/schema.sql` sin error. |
| [ ] | `08_seed_loaded.png` | Ejecucion de `db/data.sql` sin error. |
| [ ] | `09_db_tables.png` | `\dt` o lista de tablas principales. |
| [ ] | `10_db_seed_query.png` | Una consulta a `students`, `sections`, `enrollments` o `billings`. |

## 4. Servicios arriba

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `11_student_service_run.png` | Terminal de `student-service` ejecutandose. |
| [ ] | `12_course_service_run.png` | Terminal de `course-service` ejecutandose. |
| [ ] | `13_enrollment_service_run.png` | Terminal de `enrollment-service` ejecutandose. |
| [ ] | `14_billing_service_run.png` | Terminal de `billing-service` ejecutandose. |
| [ ] | `15_notification_service_run.png` | Terminal de `notification` ejecutandose. |

## 5. Health checks

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `16_health_checks.png` | Las cinco respuestas `GET /health` con `status=UP`. |

## 6. Postman

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `17_postman_collection.png` | Coleccion importada. |
| [ ] | `18_postman_environment.png` | Environment local seleccionado. |
| [ ] | `19_students_folder.png` | Carpeta `01 - Students` o una respuesta exitosa de estudiantes. |
| [ ] | `20_catalog_folder.png` | Carpeta `02 - Academic Catalog` o una respuesta exitosa del catalogo. |
| [ ] | `21_enrollment_created.png` | `POST /api/enrollments` exitoso. |
| [ ] | `22_enrollment_conflict_409.png` | Reintento del mismo `POST /api/enrollments` devolviendo `409`. |
| [ ] | `23_billing_created.png` | `POST /api/billings` exitoso. |
| [ ] | `24_billing_conflict_409.png` | Reintento del mismo `POST /api/billings` devolviendo `409`. |
| [ ] | `25_notification_health.png` | `GET /health` de `notification`. |

## 7. Redis cache

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `26_redis_ping.png` | `redis-cli ping` con `PONG`. |
| [ ] | `27_courses_cache_keys.png` | `GET /api/courses` repetido y `redis-cli --scan --pattern "courses::*"`. |
| [ ] | `28_cache_fallback_warning.png` | Warning de fallback en `course-service` cuando Redis se detiene. |

## 8. RabbitMQ y eventos

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `29_rabbitmq_ui.png` | RabbitMQ Management UI en `http://localhost:15672`. |
| [ ] | `30_rabbitmq_bindings.png` | Exchange `campusenroll.events`, queue `notification.events` y bindings. |
| [ ] | `31_enrollment_event_log.png` | `Published EnrollmentCreatedEvent ...` en `enrollment-service`. |
| [ ] | `32_billing_event_log.png` | `Published BillingStatusChangedEvent ...` en `billing-service`. |
| [ ] | `33_notification_event_logs.png` | Logs de `notification` con ambos eventos recibidos. |

## 9. k6

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `34_k6_smoke_summary.png` | Resumen de `smoke-test.js`. |
| [ ] | `35_k6_50000_summary.png` | Resumen de `load-50000-requests.js`. |
| [ ] | `36_k6_concurrent_summary.png` | Resumen de `concurrent-enrollment-test.js`. |
| [ ] | `37_k6_metrics_focus.png` | Un recorte donde se vean `total requests`, `http_req_failed`, `checks`, p95, p99 y throughput. |

## 10. Observabilidad

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `38_prometheus_targets.png` | `http://localhost:9090/targets` con `prometheus` en `UP`. |
| [ ] | `39_grafana_access.png` | Acceso a `http://localhost:3000`. |

## 11. Falla controlada

| Check | Nombre sugerido | Que capturar |
| --- | --- | --- |
| [ ] | `40_redis_stopped.png` | `docker compose stop redis` y estado del contenedor detenido. |
| [ ] | `41_courses_with_redis_down.png` | `GET /api/courses` funcionando con Redis caido. |
| [ ] | `42_redis_recovered.png` | Redis reiniciado y llaves reapareciendo. |

## 12. Mensaje final honesto

Verificar que las capturas permitan sostener estas afirmaciones:

- Redis si esta integrado en `course-service`.
- RabbitMQ si esta integrado para publicacion y consumo de eventos de evidencia.
- Prometheus y Grafana existen y se pueden abrir, pero la observabilidad de aplicacion sigue parcial.
- No existe frontend.
- Docker Compose no ejecuta los microservicios Spring Boot.
- El gateway no es el entrypoint operativo del flujo actual.
