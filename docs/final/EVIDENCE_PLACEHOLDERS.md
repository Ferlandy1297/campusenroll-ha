# Placeholders de Evidencia

Usar estos placeholders dentro de `CHECKPOINT_1_PDF_READY.md` o en el anexo del PDF final.

| Placeholder | Nombre sugerido | Que debe mostrar |
| --- | --- | --- |
| `E01` | `01_repo_root.png` | Raiz del repo con `docker-compose.yml`, `docker-compose.apps.yml` y carpetas principales. |
| `E02` | `02_standard_compose.png` | `docker compose ps` con infraestructura del standard mode arriba. |
| `E03` | `03_apps_compose_file.png` | `docker-compose.apps.yml` con servicios Spring Boot, puertos, restart y healthchecks. |
| `E04` | `04_compose_hardening.png` | Fragmento de `docker-compose.yml` con healthchecks y restart policies de infraestructura. |
| `E05` | `05_apps_compose_up.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build`. |
| `E06` | `06_apps_healthy.png` | `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps` con estados `healthy`. |
| `E07` | `07_schema_seed.png` | Ejecucion de `schema.sql` y `data.sql`. |
| `E08` | `08_db_query.png` | Consulta de datos demo en PostgreSQL. |
| `E09` | `09_service_health.png` | Los cinco `GET /health` con `status=UP`. |
| `E10` | `10_postman_import.png` | Coleccion y environment importados. |
| `E11` | `11_critical_flow.png` | Inscripcion y cobro demostrados con Postman o terminal. |
| `E12` | `12_redis_keys.png` | `courses::*` en Redis despues de dos lecturas. |
| `E13` | `13_rabbitmq_bindings.png` | Exchange, queue y bindings en RabbitMQ. |
| `E14` | `14_event_logs.png` | Logs de publicacion y consumo de eventos. |
| `E15` | `15_k6_smoke.png` | Resumen de smoke test. |
| `E16` | `16_k6_50000.png` | Resumen del escenario de 50,000 requests. |
| `E17` | `17_k6_concurrent.png` | Resumen del escenario concurrente. |
| `E18` | `18_prometheus_targets.png` | UI de Prometheus con `prometheus` y las cinco apps en `UP`. |
| `E19` | `19_actuator_prometheus.png` | Respuestas de `/actuator/prometheus` en los cinco servicios. |
| `E20` | `20_grafana_access.png` | UI de Grafana accesible. |
| `E21` | `21_course_recovery.png` | `course-service` detenido, reiniciado y saludable otra vez. |
| `E22` | `22_redis_fallback.png` | Redis detenido y `GET /api/courses` respondiendo. |
| `E23` | `23_final_summary.png` | Seccion de conclusiones o resumen final del PDF. |

## Reglas de uso

- No afirmar alta disponibilidad productiva; describir el estado como `HA-ready` y demostrable.
- No inventar replicas, balanceadores, clusters o failover si no fueron implementados.
- Si `docker-compose.apps.yml` no estaba activo, no afirmar que los cinco targets de aplicaciones estaban `UP` en Prometheus.
- Cuando la cola `notification.events` ya este consumida, combinar UI o `rabbitmqctl` con logs del consumidor.
- Si el volumen PostgreSQL era nuevo, dejar visible que `db/schema.sql` y `db/data.sql` fueron cargados.
- Si el puerto PostgreSQL local no es `5432`, dejar visible el `POSTGRES_PORT` real en alguna captura.
