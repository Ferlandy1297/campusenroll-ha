# Revision Tecnica Avanzada del Proyecto Final

Documento fuente PDF-ready para CampusEnroll HA. Este archivo esta preparado para copiarse a Word o Google Docs, completar placeholders, insertar capturas reales y exportarse como PDF.

Nota de uso:
- Sustituir todos los bloques `[Completar: ...]`.
- Insertar capturas reales donde aparezcan bloques `[Insertar evidencia ...]`.
- Renderizar los diagramas desde `docs/diagrams/` antes de la version final del PDF.
- Mantener las afirmaciones tecnicas alineadas al estado real del repositorio.

## 1. Portada

- Curso y seccion: `[Completar: curso y seccion]`
- Actividad: Revision Tecnica Avanzada del Proyecto Final
- Proyecto: CampusEnroll HA
- Segmento: S16
- Rol responsable: final PDF technical document owner
- Docente: `[Completar: nombre del docente]`
- Integrantes:
  - `[Completar: nombre completo 1]`
  - `[Completar: nombre completo 2]`
  - `[Completar: nombre completo 3]`
  - `[Completar: nombre completo 4]`
- Fecha: `[Completar: fecha de entrega]`
- URL del repositorio GitHub: `[Completar: URL del repositorio]`

[Insertar evidencia P01 - portada institucional o captura de portada final si la materia lo solicita]

## 2. Dominio asignado y contexto del problema

CampusEnroll HA aborda el dominio de gestion academica universitaria con enfasis en estudiantes, catalogo academico, secciones, inscripciones y cobros asociados. El problema principal consiste en registrar una inscripcion valida para un estudiante en una seccion academica y dejar trazable el cobro relacionado.

El repositorio no incluye frontend. Por esa razon, Postman es el cliente actual para pruebas funcionales, demostracion del flujo critico y validacion de endpoints. La arquitectura objetivo sigue el estilo de microservicios, pero el estado real del proyecto distingue con claridad entre lo implementado hoy, lo configurado en infraestructura y lo pendiente para la entrega final.

Flujo critico definido para este checkpoint:

`Inscripcion de estudiante a una seccion y generacion de cobro asociado`

### Implementado actualmente

- Existen microservicios funcionales para estudiantes, catalogo academico, inscripciones y cobros.
- Existe un esquema relacional autoritativo en PostgreSQL con datos demo.
- Existe una coleccion Postman para ejecutar el flujo actual con URLs directas por servicio.

### Configurado / preparado

- El repositorio ya contempla Redis, RabbitMQ, Prometheus y Grafana en `docker-compose.yml`.
- Existe una carpeta de gateway como componente objetivo, aunque no es el punto de entrada operativo actual.
- Existen diagramas y activos de demo para sustentar la revision tecnica.

### Pendiente para entrega final

- Integracion automatica end-to-end del flujo critico entre servicios.
- Uso real de eventos de negocio y compensaciones distribuidas.
- Cliente frontend y/o gateway operativo como entrypoint principal.

## 3. Estado actual del proyecto

### Implementado actualmente

El estado observable del repositorio demuestra una base funcional valida para cuatro dominios de negocio y un servicio de notificacion aun limitado a health check. La siguiente tabla resume el estado actual por servicio o componente cercano al dominio:

| Componente | Estado actual | Evidencia observable | Comentario tecnico |
| --- | --- | --- | --- |
| `student-service` | Implementado | `backend/student-service/`, Postman `01 - Students` | Gestiona estudiantes y cambio de estado. |
| `course-service` | Implementado | `backend/course-service/`, Postman `02 - Academic Catalog` | Gestiona cursos, periodos, secciones y bloques de horario. |
| `enrollment-service` | Implementado | `backend/enrollment-service/`, Postman `03 - Enrollments` | Gestiona inscripciones y protege duplicidad activa por estudiante-seccion. |
| `billing-service` | Implementado | `backend/billing-service/`, Postman `04 - Billings` | Gestiona cobros y protege duplicidad pendiente por inscripcion. |
| `notification` | Parcial | `backend/notification/`, Postman `05 - Notification` | Solo expone `GET /health`; no ejecuta logica de negocio ni consumidores. |
| `gateway-service` | Preparado, no operativo | `backend/gateway-service/` | Existe como carpeta/alcance del proyecto, pero no es el entrypoint usado en pruebas. |

Resumen actual de endpoints visibles por coleccion Postman:

| Servicio | URL base local directa | Endpoints observables en activos del repo | Estado |
| --- | --- | --- | --- |
| `student-service` | `http://localhost:8081` | `GET/POST /api/students`, `GET /api/students/{id}`, `PATCH /api/students/{id}/status` | Activo en pruebas |
| `course-service` | `http://localhost:8082` | `GET/POST /api/courses`, `GET /api/courses/{id}`, `GET/POST /api/periods`, `GET/POST /api/sections` | Activo en pruebas |
| `enrollment-service` | `http://localhost:8083` | `GET/POST /api/enrollments`, `GET /api/enrollments/{id}`, `PATCH /api/enrollments/{id}/status` | Activo en pruebas |
| `billing-service` | `http://localhost:8084` | `GET/POST /api/billings`, `GET /api/billings/{id}`, `PATCH /api/billings/{id}/status` | Activo en pruebas |
| `notification` | `http://localhost:8085` | `GET /health` | Solo health check |

Se debe presentar con honestidad que el flujo actual se demuestra con llamadas directas por servicio. No corresponde afirmar que existe un API Gateway activo en la ruta de negocio mostrada por Postman.

[Insertar evidencia E01 - vista raiz del repositorio]
[Insertar evidencia E02 - `git log --oneline --graph --decorate --all -n 20`]
[Insertar evidencia E03 - Postman importado con carpetas `00` a `05`]

### Configurado / preparado

- `docker-compose.yml` incluye PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana como infraestructura local.
- `db/schema.sql` y `db/data.sql` existen como entregable relacional y dataset demo.
- `infra/k6/` ya incluye scripts para smoke test, escenario de 50,000 requests, concurrencia de inscripcion y observacion manual de falla de contenedor.
- `docs/checkpoint/`, `docs/demo/` y `docs/diagrams/` ya ofrecen sustento tecnico, guion y evidencias sugeridas.

### Pendiente para entrega final

- Integracion distribuida real entre inscripcion y cobro.
- Validaciones cruzadas automaticas entre servicios antes de confirmar la inscripcion.
- Eventos de negocio con RabbitMQ y consumo real en notificacion.
- Metricas de aplicacion efectivamente expuestas y dashboards utiles.
- Evidencia final en PDF con capturas reales de ejecucion.

### Evidencia de repositorio recomendada

| Artefacto | Ruta de referencia | Valor dentro del PDF |
| --- | --- | --- |
| Microservicios | `backend/student-service/`, `backend/course-service/`, `backend/enrollment-service/`, `backend/billing-service/`, `backend/notification/`, `backend/gateway-service/` | Evidencia separacion por dominios y alcance arquitectonico |
| Infraestructura local | `docker-compose.yml` | Sustenta disponibilidad local de PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana |
| Modelo relacional | `db/schema.sql` | Sustenta el entregable autoritativo de base de datos |
| Datos demo | `db/data.sql` | Sustenta pruebas manuales coherentes |
| Cliente actual | `postman/campusenroll-ha.postman_collection.json` | Sustenta que Postman es el cliente operativo actual |
| Environment local | `postman/campusenroll-ha.local.postman_environment.json` | Sustenta configuracion reproducible por servicio |
| Pruebas de carga | `infra/k6/*.js` y `infra/k6/container-failure-observation.md` | Sustenta preparacion de validacion tecnica final |
| Checkpoint tecnico | `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md` | Sustenta el antecedente tecnico del checkpoint |
| Material de demo | `docs/demo/` | Sustenta la narrativa, comandos y checklist de evidencia |
| Diagramas fuente | `docs/diagrams/` | Sustenta arquitectura, casos de uso, secuencia, componentes, eventos y modelo ER |

## 4. Reglas criticas de negocio

### Implementado actualmente

Las reglas de negocio visibles en el repositorio y en los activos del checkpoint son las siguientes:

| ID | Regla critica | Soporte actual observable | Estado |
| --- | --- | --- | --- |
| RN-01 | No debe existir una inscripcion activa duplicada para el mismo estudiante y la misma seccion. | `enrollment-service`, Postman `03 - Enrollments`, indice parcial unico en `db/schema.sql` | Implementada |
| RN-02 | No debe existir mas de un cobro pendiente para la misma inscripcion. | `billing-service`, Postman `04 - Billings`, indice parcial unico en `db/schema.sql` | Implementada |
| RN-03 | No se deben aceptar estados de inscripcion fuera del conjunto valido. | Validacion de estado en `db/schema.sql` y servicios | Implementada |
| RN-04 | No se debe permitir un cobro con monto menor o igual a cero. | `CHECK (amount > 0)` en `db/schema.sql` | Implementada |
| RN-05 | No se debe permitir una seccion con capacidad menor o igual a cero. | `CHECK (capacity > 0)` en `db/schema.sql` | Implementada |
| RN-06 | No se debe permitir un bloque de horario con hora de fin menor o igual a hora de inicio. | `CHECK (end_time > start_time)` en `db/schema.sql` | Implementada |
| RN-07 | `student_code` debe ser unico. | Restriccion unica en `students` | Implementada |
| RN-08 | `course_code` debe ser unico. | Restriccion unica e indice case-insensitive en `courses` | Implementada |
| RN-09 | `section_code` debe ser unico dentro del periodo academico. | Restriccion unica en `sections` | Implementada |

### Configurado / preparado

- El modelo SQL ya refleja reglas clave a nivel relacional.
- La coleccion Postman permite evidenciar conflictos esperados por duplicidad.
- La documentacion del checkpoint ya separa reglas implementadas de reglas aun no distribuidas entre servicios.

### Pendiente para entrega final

- Validacion automatica de cupo disponible contra inscripciones activas al momento de la operacion.
- Validacion distribuida de existencia y estado de estudiante y seccion antes de confirmar la inscripcion.
- Validacion de traslapes de horario entre secciones desde el flujo de negocio final.
- Idempotencia formal para reintentos seguros del flujo critico.

## 5. Diagrama de arquitectura

Archivo fuente de referencia: `docs/diagrams/architecture.puml`

[Insertar diagrama D01 - arquitectura renderizada desde `architecture.puml`]

### Implementado actualmente

- Postman es el cliente de prueba real.
- Los servicios de dominio se consumen mediante URLs directas por puerto.
- PostgreSQL es el componente de persistencia realmente involucrado en el flujo actual.

### Configurado / preparado

- Redis, RabbitMQ, Prometheus y Grafana aparecen en la arquitectura local de `docker-compose.yml`.
- El gateway se mantiene como componente objetivo de arquitectura.

### Pendiente para entrega final

- Convertir al gateway en punto de entrada operativo si el equipo decide mantener ese patron.
- Conectar observabilidad y mensajeria a la ejecucion real del flujo critico.
- Ajustar el diagrama final para reflejar con exactitud la implementacion entregada.

## 6. Diagrama de casos de uso

Archivo fuente de referencia: `docs/diagrams/use-cases.puml`

[Insertar diagrama D02 - casos de uso renderizados desde `use-cases.puml`]

### Implementado actualmente

- El dominio ya permite presentar gestion de estudiantes, cursos, periodos, secciones, inscripciones y cobros.
- El caso de uso mas importante para este checkpoint es la inscripcion con cobro asociado.

### Configurado / preparado

- La narrativa del demo y la coleccion Postman ya permiten exponer estos casos de uso de forma ordenada.

### Pendiente para entrega final

- Ajustar el diagrama final si el alcance funcional crece o cambia antes de la entrega.
- Agregar actores externos adicionales solo si existen evidencias reales de implementacion.

## 7. Diagrama de secuencia del caso critico

Archivo fuente de referencia: `docs/diagrams/critical-sequence.puml`

Caso critico obligatorio:

`Inscripcion de estudiante a una seccion y generacion de cobro asociado`

[Insertar diagrama D03 - secuencia renderizada desde `critical-sequence.puml`]

### Implementado actualmente

- La demostracion real del flujo se hace con dos llamadas desde Postman: una a `POST /api/enrollments` y otra a `POST /api/billings`.
- La coordinacion actual es manual desde el cliente de prueba.
- No existe evidencia actual de publicacion de eventos ni de invocacion automatica entre servicios para este flujo.

### Configurado / preparado

- El diagrama ya puede mostrar el flujo objetivo y diferenciarlo del flujo operativo actual.
- RabbitMQ existe en infraestructura como base para una futura integracion orientada a eventos.

### Pendiente para entrega final

- Orquestacion o coreografia real entre inscripcion y cobro.
- Compensaciones ante fallas parciales para evitar estados huerfanos.
- Evidencia ejecutada del flujo final, no solo la secuencia deseada.

## 8. Diagrama de componentes

Archivo fuente de referencia: `docs/diagrams/components.puml`

[Insertar diagrama D04 - componentes renderizados desde `components.puml`]

### Implementado actualmente

- La separacion por servicios y la organizacion del repositorio son visibles.
- La coleccion Postman funciona como cliente tecnico del sistema actual.
- La base de datos entregable se presenta como artefacto transversal del dominio.

### Configurado / preparado

- La infraestructura de apoyo ya esta declarada.
- El repositorio ya contiene activos de documentacion, pruebas y diagramas para cada capa relevante.

### Pendiente para entrega final

- Validar que el diagrama final refleje la estructura definitiva de despliegue.
- Incorporar componentes reales de metricas, mensajeria o gateway solo si quedan funcionales.

## 9. Diagrama de flujo de eventos

Archivo fuente de referencia: `docs/diagrams/event-flow.puml`

[Insertar diagrama D05 - flujo de eventos renderizado desde `event-flow.puml`]

### Implementado actualmente

- No existe evidencia de eventos de negocio activos entre servicios.
- No corresponde afirmar que RabbitMQ este integrado al flujo de inscripcion o cobro.

### Configurado / preparado

- El diagrama documenta una propuesta tecnica razonable para evolucionar el sistema.
- RabbitMQ ya existe como infraestructura local separada.

### Pendiente para entrega final

- Definir contratos de eventos, exchanges, queues y politicas de reintento.
- Implementar publicadores, consumidores y trazabilidad operativa.
- Incorporar `notification` a un flujo real solo si existen consumidores implementados.

## 10. Modelo entidad-relacion

Archivos de referencia:
- `docs/diagrams/er-model.dbml`
- `db/schema.sql`

[Insertar diagrama D06 - modelo ER renderizado desde `er-model.dbml` o dbdiagram.io]

### Implementado actualmente

- El modelo relacional autoritativo incluye `students`, `courses`, `academic_periods`, `sections`, `schedule_blocks`, `enrollments` y `billings`.
- El dataset demo en `db/data.sql` permite mostrar registros coherentes para pruebas manuales.
- La relacion principal del flujo critico conecta estudiante -> inscripcion -> cobro, pasando por seccion y periodo academico.

### Configurado / preparado

- El DBML ya esta alineado con `db/schema.sql`.
- El README de `db/` explica el orden de ejecucion y la naturaleza autoritativa del esquema.

### Pendiente para entrega final

- Mantener sincronizados el DBML, `db/schema.sql` y cualquier evidencia de base de datos usada en el PDF final.
- Si el equipo ajusta el modelo antes de la entrega, regenerar la imagen final del diagrama.

## 11. Estrategia de consistencia

La estrategia de consistencia debe explicarse como una combinacion de restricciones relacionales, validaciones de servicio y trabajo pendiente para consistencia distribuida.

| Aspecto | Implementado actualmente | Configurado / preparado | Pendiente para entrega final |
| --- | --- | --- | --- |
| Integridad referencial | Claves foraneas en `db/schema.sql` | Modelo relacional estable para revision | Verificar alineacion total con runtime final |
| Dominio de estados | `CHECK` para estados de inscripcion y cobro | Coleccion Postman preparada para probar conflictos | Ajustar si aparecen estados adicionales |
| Duplicidad de inscripcion activa | Indice parcial unico en `enrollments` y validacion de servicio | Escenario k6 concurrente preparado | Idempotencia formal y validacion cruzada entre servicios |
| Duplicidad de cobro pendiente | Indice parcial unico en `billings` y validacion de servicio | Postman preparado para repetir solicitudes y observar conflicto | Manejo distribuido ante reintentos y fallas parciales |
| Transacciones de escritura | Operaciones locales por servicio | Base para evolucionar a patrones distribuidos | Saga, compensacion u outbox segun diseno final |
| Consistencia entre servicios | No automatizada en el flujo critico actual | RabbitMQ y separacion por servicios ya existen | Implementar coordinacion real, eventos y recuperacion |

### Implementado actualmente

- La consistencia fuerte actual depende principalmente de restricciones SQL y transacciones locales.
- El proyecto ya evita dos casos de inconsistencia importantes: inscripcion activa duplicada y cobro pendiente duplicado.

### Configurado / preparado

- La estructura por servicios y la existencia del broker permiten evolucionar hacia consistencia distribuida.
- El esquema SQL ya fija reglas que sirven como ultima linea de defensa.

### Pendiente para entrega final

- Idempotencia de operaciones criticas.
- Outbox o mecanismo equivalente para publicar eventos sin perdida.
- Compensaciones ante fallas entre inscripcion y cobro.

## 12. Estrategia de alta disponibilidad

La alta disponibilidad real no esta implementada todavia. Lo que existe hoy es una base local que facilita evolucionar hacia ella.

| Elemento | Implementado actualmente | Configurado / preparado | Pendiente para entrega final |
| --- | --- | --- | --- |
| Servicios de negocio | Servicios REST sin sesion de usuario en memoria visible al cliente | Diseño apto para replicas futuras | Replicas reales, balanceo y pruebas de failover |
| PostgreSQL | Contenedor dedicado con volumen persistente | Base para backups y recuperacion | Replicacion, RPO/RTO y procedimientos probados |
| Redis | Contenedor presente, sin dependencia critica actual | Puede agregarse sin romper el flujo existente | Uso real de cache y estrategia de degradacion validada |
| RabbitMQ | Contenedor presente, sin trafico de negocio actual | Base para colas, reintentos y desacoplamiento | Exchanges, queues, DLQ, monitoreo y consumidores reales |
| Observabilidad de plataforma | Prometheus y Grafana levantables por Compose | Preparacion para seguimiento tecnico | Dashboards utiles y alertas operativas |

### Implementado actualmente

- `docker-compose.yml` levanta la infraestructura base.
- Los servicios de dominio se presentan como componentes desacoplados a nivel de repositorio y endpoints.

### Configurado / preparado

- El proyecto puede evolucionar a replicas por servicio sin cambiar el cliente Postman de forma conceptual.
- La separacion de infraestructura facilita documentar una estrategia futura de recuperacion.

### Pendiente para entrega final

- Balanceo de carga y replicas reales.
- Backups, restauracion y pruebas de recuperacion para PostgreSQL.
- Plan formal de continuidad operativa mas alla del entorno local.

## 13. Estrategia de cache

No se debe afirmar que la cache ya forma parte de la logica de negocio. El estado honesto es el siguiente:

| Recurso o necesidad | Estado actual | Estrategia propuesta | Estado final esperado |
| --- | --- | --- | --- |
| Catalogo de cursos | Sin cache funcional | Redis con TTL medio y lectura frecuente | Cache de lectura con invalidacion al actualizar cursos |
| Periodos academicos | Sin cache funcional | Redis con TTL medio | Invalidacion al crear o modificar periodos |
| Disponibilidad de secciones | Sin cache funcional | Redis con TTL corto | Invalidacion al inscribir, cancelar o cambiar secciones |
| Respuesta ante caida de cache | No aplica aun | Fallback a lectura directa | Degradacion sin bloquear el flujo critico |

### Implementado actualmente

- No hay evidencia de cache integrada en los servicios de negocio.

### Configurado / preparado

- Redis esta definido en `docker-compose.yml` como componente disponible.

### Pendiente para entrega final

- Seleccionar endpoints o consultas candidatas.
- Implementar TTL, invalidacion y monitoreo.
- Medir impacto real sobre latencia y carga en base de datos.

## 14. Plan de observabilidad

La observabilidad debe presentarse como una capacidad parcialmente preparada, no como una solucion ya completada.

| Metrica o evidencia | Estado actual observable | Componente preparado | Pendiente para entrega final |
| --- | --- | --- | --- |
| Disponibilidad basica por servicio | `GET /health` en servicios activos | Postman y comandos de demo | Health consolidado y monitoreo continuo |
| Conteo de requests | No evidenciado en dashboards del repo | Prometheus presente | Exponer metricas por servicio |
| Latencia promedio, p95 y p99 | No evidenciada todavia | k6 listo para generar mediciones | Scraping, paneles y umbrales definidos |
| Errores HTTP por servicio | Observable manualmente en Postman | k6 y logs locales pueden apoyar | Paneles y alertas centralizadas |
| CPU y memoria por contenedor | Observable via Docker, no consolidado | Prometheus/Grafana presentes | Dashboards de plataforma listos |
| Conexiones PostgreSQL | No evidenciadas en paneles actuales | PostgreSQL ya forma parte del entorno | Exporter o metricas integradas |
| Backlog de colas | No aplica aun a negocio real | RabbitMQ presente | Monitoreo cuando existan eventos reales |
| Dashboards de Grafana | No se deben declarar como completos | Directorio y contenedor presentes | Datasource, paneles y validacion final |

### Implementado actualmente

- Health checks por servicio.
- Contenedores de Prometheus y Grafana declarados para entorno local.

### Configurado / preparado

- k6 ya permite producir mediciones de latencia y volumen.
- La infraestructura observacional ya tiene un punto de partida en Compose.

### Pendiente para entrega final

- Exponer metricas de aplicacion por microservicio.
- Configurar dashboards y datasource de forma util para evaluacion.
- Definir umbrales y alertas del flujo critico.

## 15. Plan de pruebas finales

El plan final debe combinar validacion funcional, concurrencia, carga y evidencia de plataforma. Los activos ya presentes en el repositorio permiten preparar esta etapa, pero no reemplazan la evidencia real que debe capturarse manualmente.

| ID | Escenario de prueba | Herramienta principal | Evidencia actual en repo | Evidencia final esperada |
| --- | --- | --- | --- | --- |
| PF-01 | Health checks de todos los servicios | Postman / `curl.exe` | Coleccion `00 - Health Checks` | Captura de respuestas exitosas |
| PF-02 | Regresion de estudiantes | Postman | Carpeta `01 - Students` | Capturas de lista, creacion o cambio de estado |
| PF-03 | Regresion de catalogo academico | Postman | Carpeta `02 - Academic Catalog` | Capturas de cursos, periodos y secciones |
| PF-04 | Flujo critico: inscripcion | Postman | Carpeta `03 - Enrollments` | `POST` exitoso y conflicto por duplicidad |
| PF-05 | Flujo critico: cobro asociado | Postman | Carpeta `04 - Billings` | `POST` exitoso y conflicto por cobro pendiente duplicado |
| PF-06 | Notification health | Postman | Carpeta `05 - Notification` | Captura honesta de `GET /health` |
| PF-07 | Smoke test de servicios | k6 | `infra/k6/smoke-test.js` | Resumen real de ejecucion y metricas |
| PF-08 | Carga acumulada de 50,000 requests | k6 | `infra/k6/load-50000-requests.js` | Resumen real con throughput y latencias |
| PF-09 | Concurrencia sobre inscripcion | k6 | `infra/k6/concurrent-enrollment-test.js` | Conflictos `409` consistentes y una sola alta valida |
| PF-10 | Observacion de falla de contenedor | Guia manual | `infra/k6/container-failure-observation.md` | Captura de procedimiento y observaciones |

### Implementado actualmente

- Existen activos para pruebas funcionales en Postman.
- Existen activos iniciales para pruebas tecnicas en k6.
- Existe dataset demo para repetir validaciones.

### Configurado / preparado

- La infraestructura local ya permite levantar base de datos, broker, cache y stack observacional.
- Los comandos de demo y evidencia ya estan documentados.

### Pendiente para entrega final

- Ejecutar las pruebas y capturar resultados reales.
- Consolidar umbrales, errores esperados y conclusiones tecnicas.
- Insertar en el PDF solo capturas verificables y legibles.

## 16. Evidencias visuales sugeridas

La version final del PDF debe contener evidencia visual suficiente para respaldar el discurso tecnico sin saturar el documento. Se recomienda insertar las capturas directamente cerca de la seccion que justifican.

| Placeholder | Evidencia sugerida | Ubicacion recomendada en este documento |
| --- | --- | --- |
| `E01` | Vista raiz del repositorio | Seccion 3 |
| `E02` | Historial de commits o `git log` | Seccion 3 |
| `E03` | Coleccion Postman importada | Seccion 3 |
| `E04` | `docker compose ps` | Seccion 3 o 12 |
| `E05` | Contenedor PostgreSQL o consulta rapida | Seccion 10 |
| `E06` | `db/schema.sql` y `db/data.sql` visibles | Seccion 10 |
| `E07` | Health checks exitosos | Seccion 14 o 15 |
| `E08` | Respuesta de `student-service` | Seccion 3 o 15 |
| `E09` | Respuesta de `course-service` | Seccion 3 o 15 |
| `E10` | Respuesta de `enrollment-service` | Seccion 7 o 15 |
| `E11` | Conflicto por duplicidad de inscripcion | Seccion 4 o 15 |
| `E12` | Respuesta de `billing-service` | Seccion 7 o 15 |
| `E13` | Conflicto por cobro pendiente duplicado | Seccion 4 o 15 |
| `E14` | `notification` health | Seccion 3 o 15 |
| `E15` | Scripts y README de `infra/k6/` | Seccion 15 |
| `E16` | Prometheus/Grafana levantados o UI disponible | Seccion 14 |
| `D01` a `D06` | Diagramas renderizados | Secciones 5 a 10 |

Para el detalle exacto de nombres sugeridos y ubicacion de cada captura, revisar `docs/final/EVIDENCE_PLACEHOLDERS.md`.

## 17. Conclusiones y plan de cierre

### Implementado actualmente

- CampusEnroll HA ya presenta una base tecnica defendible para estudiantes, catalogo, inscripciones y cobros.
- Existe un modelo relacional autoritativo con restricciones alineadas a reglas criticas.
- Existe una coleccion Postman que permite demostrar el flujo critico actual sin frontend.

### Configurado / preparado

- El repositorio ya incluye infraestructura local para PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- Existen diagramas, guion de demo, checklist de evidencia y activos k6 que fortalecen la revision tecnica.

### Pendiente para entrega final

- Integrar automaticamente el flujo critico entre servicios.
- Implementar mensajeria de negocio, observabilidad util y estrategia real de alta disponibilidad.
- Capturar evidencias finales y consolidarlas en un PDF formal de entrega.

Conclusion ejecutiva:

El proyecto ya puede demostrarse tecnicamente con honestidad como una base funcional de microservicios para el dominio academico, con soporte fuerte en base de datos y cliente de prueba por Postman. Sin embargo, la integracion distribuida completa, la cache efectiva, la observabilidad madura y la alta disponibilidad real todavia deben presentarse como trabajo pendiente y no como capacidades cerradas.

Plan de cierre sugerido antes de exportar el PDF:

1. Completar datos academicos de portada y URL del repositorio.
2. Renderizar los seis diagramas y verificar que coincidan con el estado final.
3. Ejecutar capturas manuales de Postman, Docker y base de datos.
4. Insertar evidencias en las secciones recomendadas.
5. Revisar redaccion final y exportar el PDF.
