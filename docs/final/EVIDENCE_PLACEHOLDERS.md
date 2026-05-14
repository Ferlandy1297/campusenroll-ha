# Placeholders de Evidencia

Usar estos placeholders dentro de `CHECKPOINT_1_PDF_READY.md` o en el anexo del PDF final.

| Placeholder | Nombre sugerido | Que debe mostrar |
| --- | --- | --- |
| `E01` | `01_repo_root.png` | Raiz del repo con carpetas principales. |
| `E02` | `02_compose_ps.png` | `docker compose ps` con infraestructura arriba. |
| `E03` | `03_schema_seed.png` | Ejecucion de `schema.sql` y `data.sql`. |
| `E04` | `04_db_query.png` | Consulta de datos demo en PostgreSQL. |
| `E05` | `05_service_health.png` | Los cinco `GET /health` con `status=UP`. |
| `E06` | `06_postman_import.png` | Coleccion y environment importados. |
| `E07` | `07_students_response.png` | Respuesta de estudiantes. |
| `E08` | `08_catalog_response.png` | Respuesta del catalogo academico. |
| `E09` | `09_enrollment_created.png` | Creacion exitosa de inscripcion. |
| `E10` | `10_enrollment_conflict.png` | `409` por duplicidad de inscripcion activa. |
| `E11` | `11_billing_created.png` | Creacion exitosa de cobro. |
| `E12` | `12_billing_conflict.png` | `409` por cobro pendiente duplicado. |
| `E13` | `13_redis_keys.png` | `courses::*` en Redis despues de dos lecturas. |
| `E14` | `14_rabbitmq_bindings.png` | Exchange, queue y bindings en RabbitMQ. |
| `E15` | `15_notification_logs.png` | Logs de `notification` recibiendo eventos. |
| `E16` | `16_k6_smoke.png` | Resumen de smoke test. |
| `E17` | `17_k6_50000.png` | Resumen del escenario de 50,000 requests. |
| `E18` | `18_k6_concurrent.png` | Resumen del escenario concurrente. |
| `E19` | `19_prometheus_targets.png` | UI de Prometheus con targets. |
| `E20` | `20_grafana_access.png` | UI de Grafana accesible. |
| `E21` | `21_redis_fallback.png` | Redis detenido y `GET /api/courses` respondiendo igual. |
| `E22` | `22_course_service_warning.png` | Warning de fallback en `course-service`. |
| `E23` | `23_redis_recovery.png` | Redis levantado otra vez y llaves reinsertadas. |
| `E24` | `24_final_summary.png` | Seccion de conclusiones o resumen final del PDF. |

## Reglas de uso

- No inventar dashboards, colas activas o metricas si no fueron ejecutadas localmente.
- Cuando la cola `notification.events` ya este consumida, combinar UI o `rabbitmqctl` con los logs del consumidor.
- Si el puerto PostgreSQL local no es `5432`, dejar visible el `POSTGRES_PORT` real en alguna captura.
