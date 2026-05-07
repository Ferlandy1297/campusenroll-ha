# Demo Script - Revision Tecnica Avanzada

## Objetivo general

Este guion esta pensado para una exposicion de 5 a 8 minutos. La duracion recomendada es de aproximadamente 7 minutos. El enfoque es mostrar de forma clara:

- que esta implementado actualmente
- que esta configurado o preparado
- que sigue pendiente para la entrega final

Mensaje central del demo:

- el flujo critico actual del proyecto es `Inscripcion de estudiante a una seccion y generacion de cobro asociado`
- hoy ese flujo se demuestra con Postman porque todavia no existe frontend

## Preparacion recomendada antes de iniciar

- Tener abierto el repositorio en el editor.
- Tener importados en Postman la coleccion y el environment local.
- Tener visibles `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md` y los diagramas principales.
- Tener disponible una terminal PowerShell en la raiz del repositorio.
- Si se hara demo en vivo, confirmar antes que servicios y contenedores relevantes esten levantados.

## 1. Opening - 0:00 a 0:30

Guion sugerido:

"Buenos dias. Este es el proyecto CampusEnroll HA, correspondiente a la Propuesta 5. El objetivo de esta revision tecnica avanzada es mostrar el estado actual del sistema, el flujo critico de negocio ya demostrable y la diferencia entre lo implementado, lo preparado en infraestructura y lo pendiente para la entrega final."

Que mostrar:

- nombre del repositorio
- documento `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md`

## 2. Repository overview - 0:30 a 1:00

Guion sugerido:

"El repositorio esta organizado por dominios y entregables tecnicos. En `backend/` estan los servicios, en `db/` esta el modelo SQL autoritativo y los datos demo, en `postman/` esta la coleccion funcional actual, en `infra/` estan los activos de soporte como k6, Prometheus y Grafana, y en `docs/` se concentra la documentacion del checkpoint."

Que mostrar:

- estructura de carpetas principal
- `backend/`
- `db/`
- `postman/`
- `infra/`
- `docs/`

## 3. Architecture overview - 1:00 a 1:40

Guion sugerido:

"La arquitectura objetivo es de microservicios. Actualmente Postman es el cliente operativo y consume los servicios de forma directa. El API Gateway sigue como componente documentado y planeado, pero no es todavia el punto de entrada funcional del flujo de negocio. En infraestructura local ya existen PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana mediante Docker Compose."

Que mostrar:

- `docs/diagrams/architecture.puml`
- checkpoint, secciones de arquitectura y estado actual

Mensaje clave:

- PostgreSQL participa hoy en el flujo real
- Redis, RabbitMQ, Prometheus y Grafana estan listos en infraestructura, pero no totalmente integrados a la logica de negocio

## 4. Services implemented - 1:40 a 2:40

Guion sugerido:

"En cuanto a servicios implementados, `student-service` ya gestiona estudiantes; `course-service` gestiona cursos, periodos academicos, secciones y bloques de horario; `enrollment-service` gestiona inscripciones y protege la regla de no duplicar una inscripcion activa para la misma combinacion estudiante-seccion; `billing-service` gestiona cobros y protege la regla de no duplicar cobros pendientes por inscripcion; y `notification` actualmente solo expone health check."

Que mostrar:

- README de `postman/` para orden de ejecucion
- checkpoint, seccion de estado actual

Mensaje clave:

- hay implementacion funcional de dominios principales
- no hay frontend
- `notification` aun no ejecuta consumidores ni envios reales

## 5. Database model and SQL deliverables - 2:40 a 3:20

Guion sugerido:

"En base de datos, `db/schema.sql` es el entregable autoritativo para revision. Define estudiantes, cursos, periodos, secciones, bloques de horario, inscripciones y cobros, incluyendo claves foraneas, checks e indices unicos parciales para dos reglas criticas: no duplicar inscripciones activas y no duplicar cobros pendientes. `db/data.sql` agrega un conjunto de datos demo para pruebas manuales."

Que mostrar:

- `db/README.md`
- `db/schema.sql`
- opcionalmente `db/data.sql`
- `docs/diagrams/er-model.dbml`

Mensaje clave:

- el modelo SQL esta mas normalizado y es el artefacto de revision academica
- algunos servicios todavia usan configuraciones JPA locales de desarrollo

## 6. Postman endpoint demonstration - 3:20 a 5:20

Guion sugerido:

"Como no existe frontend, Postman es el cliente actual del sistema. El flujo critico que vamos a enfatizar es la inscripcion de estudiante a una seccion y la generacion del cobro asociado. Hoy ese flujo se demuestra con dos llamadas directas: primero a `enrollment-service` y luego a `billing-service`."

Orden sugerido del demo:

1. Mostrar la coleccion y el environment local.
2. Ejecutar `00 - Health Checks`.
3. Mostrar rapidamente `01 - Students` y `02 - Academic Catalog`.
4. Ejecutar o mostrar `POST /api/enrollments` con `studentId` y `sectionId`.
5. Repetir el mismo `POST /api/enrollments` para evidenciar el rechazo por duplicidad activa.
6. Ejecutar o mostrar `POST /api/billings` con `enrollmentId`, `amount`, `currency` y `status=PENDING`.
7. Repetir el mismo `POST /api/billings` para evidenciar el rechazo por duplicidad pendiente.
8. Mostrar `05 - Notification` solo como health check.

Frases sugeridas durante la demostracion:

- "Aqui se observa la creacion o consulta de estudiantes."
- "Aqui se observa el catalogo academico: cursos, periodos y secciones."
- "Este `POST /api/enrollments` representa la inscripcion actual."
- "Al repetir la solicitud, el sistema responde con conflicto porque ya existe una inscripcion activa."
- "Luego se genera el cobro asociado en `billing-service`."
- "Al repetir el cobro pendiente para la misma inscripcion, el sistema tambien responde con conflicto."

Mensaje clave:

- el flujo critico hoy es funcional, pero todavia no es un flujo distribuido automatico
- la coordinacion sigue siendo manual desde Postman

## 7. k6 testing plan - 5:20 a 5:50

Guion sugerido:

"En pruebas de carga, el repositorio ya incluye activos k6 para smoke test, un escenario acumulado de 50,000 requests, una prueba concurrente de inscripcion y una guia de observacion de falla de contenedor. Es importante aclarar que estos activos existen y estan listos para ejecucion, pero el repositorio no afirma que ya hayan sido corridos como evidencia final."

Que mostrar:

- `infra/k6/README.md`
- nombres de los scripts disponibles

Mensaje clave:

- k6 esta preparado para la siguiente fase de validacion
- la evidencia real debe producirse al ejecutar las pruebas

## 8. Infrastructure: PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana - 5:50 a 6:20

Guion sugerido:

"A nivel de infraestructura, Docker Compose ya levanta PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana. Actualmente PostgreSQL es el componente directamente involucrado en el flujo de negocio demostrado. Redis y RabbitMQ estan listos, pero no integrados plenamente a la logica actual. Prometheus y Grafana existen en el entorno, aunque todavia no podemos afirmar que haya dashboards completos ni observabilidad de negocio totalmente cableada."

Que mostrar:

- `docker compose ps`
- `docker-compose.yml`

Mensaje clave:

- infraestructura lista para crecer
- integracion funcional aun parcial fuera de PostgreSQL

## 9. Honest pending work - 6:20 a 6:50

Guion sugerido:

"De manera honesta, quedan pendientes el API Gateway operativo, la integracion automatica entre inscripcion y cobro, la validacion cruzada entre servicios, la publicacion y consumo real de eventos, la estrategia completa de idempotencia, la observabilidad con metricas de aplicacion y una estrategia de alta disponibilidad mas alla del entorno local."

Que mostrar:

- checkpoint, secciones de pendientes

Mensaje clave:

- el proyecto ya demuestra base funcional y artefactos tecnicos
- todavia no debe presentarse como sistema completamente integrado de extremo a extremo

## 10. Closing statement - 6:50 a 7:10

Guion sugerido:

"En conclusion, CampusEnroll HA ya cuenta con servicios funcionales para estudiantes, catalogo, inscripciones y cobros, un modelo relacional autoritativo, activos de prueba y una base de infraestructura local util para el checkpoint. El flujo critico actual es demostrable con Postman, mientras que la integracion distribuida completa, la observabilidad avanzada y la alta disponibilidad real quedan como trabajo pendiente para la entrega final."

## Cierre practico del demo

Si el tiempo se reduce a 5 o 6 minutos, priorizar:

1. overview del repositorio
2. arquitectura
3. flujo critico por Postman
4. base de datos
5. pendientes honestos

Si el tiempo se acerca a 8 minutos, agregar:

1. mas detalle sobre k6
2. mas detalle sobre infraestructura
3. referencia breve a diagramas del checkpoint
