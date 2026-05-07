# Placeholders de Evidencia

Este archivo lista las evidencias visuales que deben capturarse manualmente antes de cerrar el PDF final. Los nombres sugeridos ayudan a mantener ordenadas las imagenes.

## Tabla de placeholders

| Placeholder | Screenshot sugerida | Que capturar | Insertar en `CHECKPOINT_1_PDF_READY.md` |
| --- | --- | --- | --- |
| `E01` | `01_repository_root.png` | Vista de la raiz del repositorio mostrando `backend/`, `db/`, `docs/`, `infra/`, `postman/` y, si es posible, el nombre del proyecto. | Seccion 3. Estado actual del proyecto |
| `E02` | `02_git_log.png` | Salida de `git log --oneline --graph --decorate --all -n 20` o evidencia equivalente del historial reciente. | Seccion 3. Estado actual del proyecto |
| `E03` | `03_docker_compose_ps.png` | Salida de `docker compose ps` con los contenedores principales visibles. | Seccion 3 o 12 |
| `E04` | `04_postgresql_container.png` | Contenedor `campusenroll-postgres` visible en Docker Desktop, terminal o una consulta simple dentro del contenedor. | Seccion 10. Modelo entidad-relacion |
| `E05` | `05_db_schema_and_data.png` | `db/schema.sql` y `db/data.sql` visibles en el editor o evidencia de tablas y datos cargados. | Seccion 10. Modelo entidad-relacion |
| `E06` | `06_postman_collection_imported.png` | Coleccion `campusenroll-ha.postman_collection.json` importada en Postman, con carpetas `00` a `05`. | Seccion 3. Estado actual del proyecto |
| `E07` | `07_health_endpoints.png` | Ejecucion de health checks de los servicios implementados, preferiblemente desde la carpeta `00 - Health Checks` de Postman. | Seccion 14 o 15 |
| `E08` | `08_student_endpoint_response.png` | Respuesta exitosa de `GET /api/students`, `POST /api/students` o `PATCH /api/students/{id}/status`. | Seccion 3 o 15 |
| `E09` | `09_course_catalog_response.png` | Respuesta de catalogo academico, por ejemplo `GET /api/courses`, `GET /api/periods` o `GET /api/sections`. | Seccion 3 o 15 |
| `E10` | `10_enrollment_response.png` | `POST /api/enrollments` exitoso o `GET /api/enrollments/{id}` mostrando una inscripcion valida. | Seccion 7 o 15 |
| `E11` | `11_billing_response.png` | `POST /api/billings` exitoso o `GET /api/billings/{id}` mostrando un cobro asociado. | Seccion 7 o 15 |
| `E12` | `12_notification_health.png` | Respuesta de `GET /health` del servicio `notification`. | Seccion 3 o 15 |
| `E13` | `13_k6_files.png` | `infra/k6/README.md` y los archivos `smoke-test.js`, `load-50000-requests.js`, `concurrent-enrollment-test.js` visibles en el editor o explorador. | Seccion 15. Plan de pruebas finales |
| `E14` | `14_prometheus_grafana.png` | Contenedores Prometheus/Grafana en `docker compose ps` o sus UIs si estan disponibles y accesibles. | Seccion 14. Plan de observabilidad |
| `D01` | `15_architecture_rendered.png` | Diagrama renderizado de `docs/diagrams/architecture.puml`. | Seccion 5. Diagrama de arquitectura |
| `D02` | `16_use_cases_rendered.png` | Diagrama renderizado de `docs/diagrams/use-cases.puml`. | Seccion 6. Diagrama de casos de uso |
| `D03` | `17_critical_sequence_rendered.png` | Diagrama renderizado de `docs/diagrams/critical-sequence.puml`. | Seccion 7. Diagrama de secuencia del caso critico |
| `D04` | `18_components_rendered.png` | Diagrama renderizado de `docs/diagrams/components.puml`. | Seccion 8. Diagrama de componentes |
| `D05` | `19_event_flow_rendered.png` | Diagrama renderizado de `docs/diagrams/event-flow.puml`. | Seccion 9. Diagrama de flujo de eventos |
| `D06` | `20_er_model_rendered.png` | Modelo ER renderizado desde `docs/diagrams/er-model.dbml` en dbdiagram.io o herramienta equivalente. | Seccion 10. Modelo entidad-relacion |

## Recomendaciones de captura

- Asegurar buena resolucion y texto legible.
- Evitar capturas recortadas sin contexto.
- Mostrar nombre de archivo, endpoint o comando cuando aporte trazabilidad.
- Si una sola captura cubre dos evidencias relacionadas, mantener el nombre de la evidencia principal y explicarlo en el PDF.
- Si se agregan capturas adicionales, seguir la misma convencion numerica.

## Evidencias manuales que no debe inventar el documento

- No usar capturas de dashboards inexistentes como si fueran funcionales.
- No usar capturas de eventos RabbitMQ si no hay trafico real implementado.
- No usar capturas de cache Redis como si ya participara en la logica de negocio.
- No usar una ruta de gateway como si fuera el entrypoint activo, salvo que exista evidencia real y actualizada.
