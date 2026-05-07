# Checkpoint 1 - Revision Tecnica Avanzada

## 1. Portada

- Proyecto: CampusEnroll HA
- Segmento: S13
- Actividad: Revision Tecnica Avanzada del Proyecto Final
- Rol responsable: checkpoint documentation and diagrams owner
- Fecha de elaboracion: 2026-05-06
- Estado del documento: alineado al estado implementado actual del repositorio

## 2. Dominio asignado y contexto del problema

CampusEnroll HA aborda el dominio de gestion academica universitaria con foco en la administracion de estudiantes, catalogo academico, secciones, inscripciones y cobros asociados. El problema principal consiste en registrar de forma consistente la inscripcion de un estudiante a una seccion academica y asegurar que el cobro relacionado quede trazable dentro del mismo flujo de negocio.

El repositorio actual no incluye frontend. Por tanto, el cliente operativo de este segmento es Postman, utilizado para validacion funcional directa contra los microservicios expuestos. La arquitectura objetivo sigue siendo de microservicios, pero todavia existe una separacion clara entre lo implementado hoy, la infraestructura ya preparada y las capacidades pendientes para la entrega final.

## 3. Estado actual del proyecto

### Implementado actualmente

- `student-service` expone endpoints para estudiantes: listado, consulta por id, creacion y actualizacion de estado.
- `course-service` expone endpoints para cursos, periodos academicos, secciones y bloques de horario.
- `enrollment-service` expone endpoints de inscripcion y valida duplicidad de inscripcion activa para la combinacion estudiante-seccion.
- `billing-service` expone endpoints de cobro y valida duplicidad de cobro pendiente por inscripcion.
- `notification` solo expone `GET /health`; no tiene logica de negocio ni consumidores de eventos.
- `db/schema.sql` y `db/data.sql` existen como entregable relacional y set de datos demo para revision.
- La coleccion y el environment de Postman existen y usan URLs directas por servicio.
- `docker-compose.yml` levanta PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana para entorno local.
- No existe frontend implementado en el repositorio.

### Configurado / preparado

- Redis esta disponible en infraestructura local, pero aun no participa en la logica de negocio.
- RabbitMQ esta disponible en infraestructura local, pero aun no hay publicacion ni consumo de eventos de negocio.
- Prometheus esta presente con configuracion base y actualmente se auto-observa; aun no recolecta metricas de los servicios de negocio.
- Grafana esta presente como contenedor y con directorio de provisioning montado, pero no trae datasource ni dashboards funcionales listos en el repositorio.
- El API Gateway existe como documentacion y carpeta de servicio, pero no como punto de entrada funcional usado por Postman.

### Pendiente para entrega final

- Integracion real entre inscripcion y cobro dentro de un flujo distribuido.
- Validaciones cruzadas entre `enrollment-service`, `student-service` y `course-service`.
- Publicacion de eventos, orquestacion por saga y compensaciones.
- Idempotencia formal para operaciones criticas.
- Exposicion y scraping real de metricas de aplicacion.
- Estrategia de replicas y recuperacion mas alla del entorno local de Compose.

## 4. Reglas criticas de negocio

Las reglas criticas identificadas para el checkpoint son las siguientes:

1. No debe existir una inscripcion activa duplicada para el mismo estudiante y la misma seccion.
2. No se deben aceptar estados de inscripcion fuera del conjunto valido definido por el servicio.
3. No debe existir mas de un cobro pendiente para la misma inscripcion.
4. No se debe permitir un cobro con monto menor o igual a cero.
5. No se debe permitir una seccion con capacidad menor o igual a cero.
6. No se debe permitir un bloque de horario donde la hora de fin sea menor o igual a la hora de inicio.

Reglas adicionales ya visibles en el modelo entregable:

- `student_code` debe ser unico en estudiantes.
- `course_code` debe ser unico en cursos.
- `section_code` debe ser unico dentro de un mismo periodo academico.

## 5. Diagrama de arquitectura

Archivo fuente: [`docs/diagrams/architecture.puml`](../diagrams/architecture.puml)

Lectura tecnica:

- Postman es el cliente actual de prueba y consume los servicios de forma directa.
- El API Gateway se mantiene como componente objetivo de arquitectura, pero sigue pendiente como entry point operativo.
- Los microservicios implementados se apoyan en PostgreSQL para persistencia.
- Redis, RabbitMQ, Prometheus y Grafana ya existen en la infraestructura local, aunque su nivel de integracion funcional es dispar.
- `docker-compose.yml` representa hoy la base de infraestructura compartida, no el despliegue completo de todos los microservicios.

## 6. Diagrama de casos de uso

Archivo fuente: [`docs/diagrams/use-cases.puml`](../diagrams/use-cases.puml)

Casos de uso incluidos:

- Gestionar estudiantes
- Gestionar cursos
- Gestionar secciones
- Inscribir estudiante
- Generar cobro
- Consultar estado de inscripcion

Caso critico del checkpoint:

- `Inscribir estudiante`, con relacion directa a la generacion de cobro asociado.

## 7. Diagrama de secuencia del caso critico

Archivo fuente: [`docs/diagrams/critical-sequence.puml`](../diagrams/critical-sequence.puml)

Caso critico definido:

- `Inscripcion de estudiante a una seccion y generacion de cobro asociado`

Interpretacion del estado actual:

- Hoy la secuencia operativa real se resuelve con dos llamadas separadas desde Postman: primero a `enrollment-service` y luego a `billing-service`.
- Actualmente no existe una llamada implementada desde `enrollment-service` hacia `student-service` ni hacia `course-service` para validar existencia, estado, cupo o traslapes.
- Tampoco existe publicacion real de eventos a RabbitMQ ni consumo en `notification`.

Interpretacion del estado objetivo:

- Para la entrega final se proyecta una validacion distribuida previa a la inscripcion, seguida por coordinacion entre inscripcion y cobro mediante eventos y compensacion.

## 8. Diagrama de componentes

Archivo fuente: [`docs/diagrams/components.puml`](../diagrams/components.puml)

El diagrama muestra:

- La estructura interna comun de los servicios implementados: controladores REST, capa de servicio y repositorios.
- La relacion de cada servicio con PostgreSQL.
- La coleccion Postman como cliente funcional actual.
- Docker Compose como contenedor de infraestructura local compartida.
- Los componentes de observabilidad disponibles hoy y su caracter aun parcial.

## 9. Diagrama de flujo de eventos

Archivo fuente: [`docs/diagrams/event-flow.puml`](../diagrams/event-flow.puml)

Este diagrama representa una propuesta de mensajeria para la entrega final y no una capacidad productiva ya implementada. Los eventos incluidos son:

- `EnrollmentCreated`
- `BillingCreated`
- `BillingPaid`
- `NotificationRequested`

Los nombres de exchanges y queues incluidos en el diagrama deben entenderse como placeholders propuestos para formalizar la integracion futura.

## 10. Modelo entidad-relacion

Archivo fuente: [`docs/diagrams/er-model.dbml`](../diagrams/er-model.dbml)

Entidades incluidas en el modelo:

- `students`
- `courses`
- `academic_periods`
- `sections`
- `schedule_blocks`
- `enrollments`
- `billings`

Relaciones principales:

- una seccion pertenece a un curso
- una seccion pertenece a un periodo academico
- una seccion tiene uno o varios bloques de horario en el modelo funcional
- una inscripcion referencia a un estudiante y a una seccion
- un cobro referencia a una inscripcion

Observacion importante:

- `db/schema.sql` es el modelo relacional autoritativo del entregable de base de datos.
- El runtime local de algunos servicios todavia conserva diferencias de persistencia por configuraciones JPA de desarrollo; por ello este checkpoint separa explicitamente el modelo entregable de BD del estado de integracion actual entre servicios.

## 11. Estrategia de consistencia

### Implementado actualmente

- Restricciones de base de datos en `db/schema.sql`: claves primarias, claves foraneas y checks de dominio.
- Indices unicos e indices unicos parciales en el modelo SQL para proteger unicidad relevante del negocio.
- Transacciones locales en operaciones de escritura dentro de `course-service`, `enrollment-service` y `billing-service`.
- Validacion de estados por enumeraciones y validacion de payloads en los servicios.
- Validacion de duplicidad activa en inscripciones y duplicidad pendiente en cobros a nivel de servicio.

### Configurado / preparado

- El modelo SQL ya deja preparadas restricciones para endurecer la consistencia del dominio desde PostgreSQL.
- La separacion por servicios permite evolucionar hacia consistencia por coordinacion distribuida sin acoplar reglas a un monolito.

### Pendiente para entrega final

- Idempotencia formal en endpoints criticos para reintentos seguros.
- Saga o compensacion para el caso `inscripcion + cobro`, de forma que no queden estados huerfanos ante fallas parciales.
- Patron outbox para publicar eventos de manera confiable desde transacciones locales.
- Validacion cruzada automatizada de existencia y estado de estudiante y seccion antes de confirmar la inscripcion.

## 12. Estrategia de alta disponibilidad

### Implementado actualmente

- Existe un entorno local con Docker Compose para infraestructura compartida: PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- Los microservicios implementados son stateless respecto al cliente: exponen APIs REST y no dependen de sesion de usuario en memoria.
- La configuracion principal de puertos y conexiones esta externalizada por variables de entorno.

### Configurado / preparado

- La naturaleza stateless de los servicios permite proyectar replicas futuras sin cambiar el contrato de cliente.
- PostgreSQL ya esta aislado como servicio propio y con volumen persistente, lo que facilita una evolucion hacia backups automatizados y replicacion.
- Redis, al no ser dependencia critica de negocio hoy, puede degradarse sin impedir el flujo principal actual.
- RabbitMQ ya existe como broker separado, lo que habilita una estrategia futura de retries y dead-letter queues.

### Pendiente para entrega final

- Replicas reales de microservicios y balanceo de trafico.
- Politica formal de backups, replicacion y recuperacion para PostgreSQL.
- Estrategia de failover documentada y probada para servicios de negocio.
- Retries, dead-letter queues y observacion de backlog para RabbitMQ.
- Automatizacion de recuperacion ante caida de contenedores mas alla del entorno local.

## 13. Estrategia de cache

### Implementado actualmente

- No hay cache funcional integrada en la logica de negocio.
- Las lecturas actuales se resuelven contra PostgreSQL o la persistencia local administrada por cada servicio.

### Configurado / preparado

- Redis ya esta configurado como componente de infraestructura en Docker Compose.
- Esto permite incorporar cache sin redisenar la topologia general del proyecto.

### Pendiente para entrega final

- Candidatos de cache:
  - catalogo de cursos
  - periodos academicos
  - resumen de disponibilidad de secciones
- Politica de TTL propuesta:
  - catalogo de cursos y periodos: TTL medio, por ejemplo 5 a 15 minutos
  - disponibilidad de secciones: TTL corto, por ejemplo 30 a 60 segundos
- Politica de invalidacion propuesta:
  - invalidar catalogo al crear o actualizar cursos
  - invalidar periodos al crear o actualizar periodos academicos
  - invalidar disponibilidad de secciones al crear/cancelar inscripciones o modificar secciones
- Estrategia de degradacion:
  - si Redis no esta disponible, el sistema debe volver a lecturas directas sin bloquear operaciones criticas

## 14. Plan de observabilidad

### Implementado actualmente

- Todos los servicios activos exponen `GET /health`.
- Prometheus y Grafana estan presentes en la infraestructura local.
- Prometheus cuenta con configuracion base y actualmente solo se scrapea a si mismo.

### Configurado / preparado

- Los servicios incluyen Spring Boot Actuator como dependencia, pero la exposicion de endpoints de management permanece deshabilitada por defecto.
- Grafana ya tiene un directorio de provisioning montado para evolucionar a configuracion automatizada.

### Pendiente para entrega final

- Exponer y recolectar metricas de aplicacion por servicio.
- Registrar datasource de Prometheus en Grafana.
- Construir dashboards minimos por dominio y por infraestructura.
- Definir alertas operativas.

Metricas objetivo para seguimiento:

- request count
- latencia promedio
- latencia p95
- latencia p99
- errores HTTP por codigo y por servicio
- uso de CPU y memoria por contenedor
- health de contenedores
- conexiones activas a PostgreSQL
- backlog y fallas de eventos en RabbitMQ

## 15. Plan de pruebas finales

### Implementado actualmente

- La coleccion Postman y su environment local ya existen y cubren health checks y operaciones de los servicios implementados.
- `db/data.sql` entrega un conjunto de datos coherente para pruebas manuales y demostracion.

### Configurado / preparado

- El repositorio ya contempla infraestructura suficiente para pruebas tecnicas sobre PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- El directorio `infra/k6/` existe como base para la siguiente fase de carga, aunque la ejecucion de carga aun no forma parte de este segmento.

### Pendiente para entrega final

Escenarios de prueba propuestos:

1. Regresion funcional con Postman.
   - Objetivo: validar endpoints, estados y reglas de negocio actuales.
   - Resultado esperado: respuestas correctas, errores 4xx cuando apliquen y ausencia de errores 5xx inesperados.

2. Prueba de carga planificada con k6 sobre 50,000 requests.
   - Objetivo: medir estabilidad del sistema bajo volumen acumulado.
   - Resultado esperado: tasa de error controlada, latencia p95 y p99 dentro de umbrales acordados para la entrega final, sin corrupcion de datos.
   - Metricas a revisar: request count, throughput, latencia promedio/p95/p99, errores HTTP, CPU, memoria y conexiones PostgreSQL.

3. Escenario concurrente de inscripcion.
   - Objetivo: lanzar multiples intentos sobre la misma combinacion estudiante-seccion.
   - Resultado esperado: solo una inscripcion activa valida y rechazo consistente del resto por regla de duplicidad.
   - Metricas a revisar: tasa de conflictos esperados, consistencia final de datos y tiempo de respuesta.

4. Escenario concurrente de generacion de cobro.
   - Objetivo: lanzar multiples intentos de cobro pendiente para la misma inscripcion.
   - Resultado esperado: solo un cobro pendiente vigente por inscripcion y rechazo consistente del resto.
   - Metricas a revisar: conflictos esperados, integridad de billings y latencia.

5. Escenario de falla de contenedor.
   - Objetivo: detener un contenedor relevante y observar recuperacion operativa.
   - Resultado esperado: perdida controlada del componente afectado, recuperacion por reinicio donde aplique y sin dano permanente a la integridad de datos.
   - Metricas a revisar: health de contenedores, reinicios, disponibilidad percibida y backlog de eventos cuando exista mensajeria real.

Conclusion del checkpoint:

- El proyecto ya cuenta con una base funcional valida para demostrar dominio, modelo relacional y servicios nucleares.
- La integracion distribuida, la observabilidad completa, la cache operativa y la alta disponibilidad real siguen siendo trabajo pendiente para la entrega final.
