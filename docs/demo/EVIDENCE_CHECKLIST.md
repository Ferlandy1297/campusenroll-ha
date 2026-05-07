# Evidence Checklist - Revision Tecnica Avanzada

## Uso de este checklist

Este checklist sirve para preparar las capturas y evidencias antes de enviar el checkpoint. Las capturas deben hacerse manualmente. El nombre sugerido ayuda a mantener orden en la carpeta de evidencias o en el PDF final.

## 1. GitHub repository

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `01_repo_home.png` | Vista principal del repositorio con nombre del proyecto y rama visible. | Demuestra existencia, identidad del proyecto y control del repositorio. |
| [ ] | `02_repo_readme_or_docs.png` | README o carpeta `docs/` visible desde la plataforma remota o local. | Evidencia que el proyecto esta documentado y organizado. |

## 2. Branches / PRs / commits

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `03_branch_list.png` | Lista de ramas o al menos la rama actual usada para el checkpoint. | Demuestra orden de trabajo y trazabilidad tecnica. |
| [ ] | `04_commit_history.png` | Historial de commits recientes con mensajes legibles. | Permite ver avance incremental y disciplina de versionado. |
| [ ] | `05_pr_or_no_pr_evidence.png` | Si existe PR, capturarlo; si no existe, mostrar evidencia equivalente de revision local o commit log. | Sustenta el proceso de trabajo aunque no haya flujo formal completo. |

## 3. Project structure

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `06_repo_structure.png` | Estructura raiz mostrando `backend/`, `db/`, `docs/`, `infra/` y `postman/`. | Demuestra organizacion por dominios y entregables tecnicos. |
| [ ] | `07_backend_services.png` | Contenido de `backend/` con servicios visibles. | Confirma separacion por microservicios. |

## 4. Docker Compose / containers

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `08_compose_ps.png` | Salida de `docker compose ps`. | Evidencia infraestructura local levantada para el checkpoint. |
| [ ] | `09_compose_file.png` | Seccion relevante de `docker-compose.yml` mostrando PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana. | Confirma que la infraestructura declarada existe en el repo. |

## 5. PostgreSQL / database evidence

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `10_db_readme.png` | `db/README.md` con explicacion del entregable SQL. | Muestra que el modelo relacional fue documentado y contextualizado. |
| [ ] | `11_schema_tables.png` | Vista de `db/schema.sql` o de tablas creadas en PostgreSQL. | Evidencia el modelo de datos entregable. |
| [ ] | `12_db_seed_or_query.png` | Resultado de consulta como `SELECT` sobre `students`, `sections` o `billings`. | Demuestra datos de prueba y consistencia basica del modelo. |

## 6. Postman collection

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `13_postman_collection.png` | Coleccion importada con carpetas `00` a `05`. | Demuestra que existe un cliente operativo real para el sistema actual. |
| [ ] | `14_postman_environment.png` | Environment local con URLs por servicio. | Muestra configuracion reproducible de pruebas manuales. |

## 7. Service health endpoints

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `15_health_checks.png` | Ejecucion de `00 - Health Checks` con respuestas exitosas. | Confirma disponibilidad basica de los servicios implementados. |

## 8. Student endpoints

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `16_students_list_or_create.png` | `GET /api/students` o `POST /api/students` exitoso. | Evidencia funcionamiento del dominio de estudiantes. |
| [ ] | `17_student_status_update.png` | `PATCH /api/students/{id}/status` o respuesta equivalente. | Muestra soporte de operaciones adicionales al CRUD minimo visible. |

## 9. Course / catalog endpoints

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `18_courses_endpoint.png` | `GET` o `POST /api/courses`. | Demuestra gestion del catalogo de cursos. |
| [ ] | `19_periods_endpoint.png` | `GET` o `POST /api/periods`. | Evidencia gestion de periodos academicos. |
| [ ] | `20_sections_endpoint.png` | `GET` o `POST /api/sections` con bloque de horario visible. | Muestra gestion de secciones y horarios. |

## 10. Enrollment endpoints

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `21_enrollment_create.png` | `POST /api/enrollments` exitoso con `studentId` y `sectionId`. | Demuestra el inicio del flujo critico actual. |
| [ ] | `22_enrollment_duplicate_conflict.png` | Repeticion del mismo `POST /api/enrollments` mostrando conflicto. | Evidencia la regla critica de no duplicar inscripcion activa. |
| [ ] | `23_enrollment_status.png` | `GET /api/enrollments/{id}` o `PATCH /api/enrollments/{id}/status`. | Permite ver estado y trazabilidad de la inscripcion. |

## 11. Billing endpoints

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `24_billing_create.png` | `POST /api/billings` exitoso asociado a una inscripcion. | Demuestra la segunda parte del flujo critico actual. |
| [ ] | `25_billing_duplicate_pending_conflict.png` | Repeticion del cobro pendiente mostrando conflicto. | Evidencia la regla critica de no duplicar cobro pendiente. |
| [ ] | `26_billing_status.png` | `GET /api/billings/{id}` o `PATCH /api/billings/{id}/status`. | Muestra seguimiento del estado del cobro. |

## 12. Notification health check

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `27_notification_health.png` | `GET /health` de `notification`. | Permite demostrar honestamente que el servicio existe, aunque solo tenga health check. |

## 13. Diagrams

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `28_architecture_diagram.png` | Diagrama de arquitectura o su fuente `.puml`. | Refuerza comprension de componentes y estado actual. |
| [ ] | `29_sequence_diagram.png` | Diagrama de secuencia del caso critico. | Sustenta tecnicamente el flujo de inscripcion y cobro. |
| [ ] | `30_er_model.png` | DBML o diagrama ER visible. | Conecta la parte de servicios con el modelo relacional. |

## 14. k6 assets

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `31_k6_readme.png` | `infra/k6/README.md` con escenarios listados. | Demuestra preparacion para pruebas de carga y concurrencia. |
| [ ] | `32_k6_scripts.png` | Archivos `smoke-test.js`, `load-50000-requests.js`, `concurrent-enrollment-test.js`. | Evidencia que el plan tecnico de pruebas tiene activos concretos. |
| [ ] | `33_k6_execution_optional.png` | Si se ejecuta, capturar resumen de k6; si no, dejar claro que esta pendiente. | Diferencia entre activos preparados y evidencia real ejecutada. |

## 15. Pending features and risks

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `34_pending_work_checkpoint.png` | Seccion de pendientes honestos del checkpoint. | Demuestra transparencia tecnica y evita sobredeclarar implementacion. |
| [ ] | `35_risks_no_frontend_gateway_events.png` | Evidencia documental de que no hay frontend, gateway operativo ni mensajeria de negocio integrada. | Ayuda a explicar limites reales del estado actual. |

## 16. Final PDF evidence

| Check | Screenshot sugerida | Que capturar | Por que importa para la calificacion |
| --- | --- | --- | --- |
| [ ] | `36_pdf_cover.png` | Portada del PDF final de evidencia. | Presenta el entregable final de forma formal. |
| [ ] | `37_pdf_index_or_sections.png` | Indice o secciones del PDF con orden claro. | Facilita revision por parte del evaluador. |
| [ ] | `38_pdf_appendix_evidence.png` | Anexo de capturas finales integradas en el PDF. | Demuestra que la evidencia esta consolidada y lista para evaluacion. |

## Recomendacion final

Antes de enviar:

1. verificar que las capturas tengan fecha o contexto visible cuando sea posible
2. usar nombres ordenados para las imagenes
3. evitar capturas ambiguas o recortadas
4. aclarar explicitamente que Postman es el cliente actual y que no existe frontend todavia
5. no presentar Redis, RabbitMQ o dashboards de Grafana como integraciones de negocio ya completadas
