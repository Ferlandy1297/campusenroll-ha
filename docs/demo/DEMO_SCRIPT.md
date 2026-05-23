# Demo Script - Entrega Final S31

## Objetivo

Exponer en 7 a 9 minutos el estado real de CampusEnroll HA despues de S25, sin sobredeclarar cluster, failover de base de datos, backups productivos ni observabilidad de nivel productivo que el repo aun no entrega.

Mensaje central:

`CampusEnroll HA ya tiene flujo funcional, cache Redis, eventos RabbitMQ, healthchecks, modo Compose HA-ready, metricas Prometheus reales por microservicio, backup/restore local de PostgreSQL, alertas activas en Prometheus, Idempotency-Key real para escrituras criticas, outbox transaccional para los productores de eventos y ahora una compensacion basica por choreografia cuando un billing queda CANCELLED; eso no equivale todavia a alta disponibilidad productiva, a un motor de saga completo ni a failover de base de datos.`

## 0. Preparacion previa

Antes de iniciar la demo:

- infraestructura arriba con `docker-compose.yml` o el stack completo con `docker-compose.apps.yml`
- `db/schema.sql` y `db/data.sql` cargados si el volumen es nuevo
- conocer al menos un dump valido dentro de `infra/backups/output/` si se mostrara la parte de restore
- elegir una ruta para apps:
  - servicios con `mvn spring-boot:run`, o
  - servicios con `docker-compose.apps.yml`
- Postman importado
- RabbitMQ UI, Prometheus y Grafana accesibles
- una terminal PowerShell abierta en la raiz del repo
- recordar que no existe frontend; Postman sigue siendo el cliente operativo

## 1. Apertura - 0:00 a 0:40

Guion sugerido:

"Este es CampusEnroll HA. La base actual ya permite demostrar estudiantes, catalogo, inscripciones y cobros. S20 agrego el modo Compose para levantar tambien los cinco microservicios Spring Boot con restart policy y healthchecks. S21 completo esa base con metricas Prometheus reales en los cinco servicios. S22 agrega backup, restore y un runbook de recuperacion para PostgreSQL. S25 suma failover y switchover a nivel de aplicacion para course-service usando HAProxy y una replica. S28 activa reglas reales de Prometheus, S29 agrega Idempotency-Key para escrituras criticas, S30 agrega outbox transaccional para los servicios que producen eventos y S31 agrega una compensacion basica por choreografia cuando el billing queda CANCELLED."

Mostrar:

- `README.md`
- `docs/final/CHECKPOINT_1_PDF_READY.md`

## 2. Standard mode y HA readiness mode - 0:40 a 1:30

Guion sugerido:

"Ahora el repo tiene tres capas claras. El standard mode mantiene `docker-compose.yml` para infraestructura compartida. El HA readiness mode agrega `docker-compose.apps.yml` para contenedorizacion local de los cinco servicios. Y S25 suma `docker-compose.ha-demo.yml` para el demo de continuidad del catalogo con HAProxy."

Mostrar:

- `docker-compose.yml`
- `docker-compose.apps.yml`
- `docker-compose.ha-demo.yml`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps`

## 3. Base de datos determinista y verificacion S22 - 1:30 a 2:20

Guion sugerido:

"La base demo se reinicia con `db/schema.sql` y `db/data.sql`. Eso deja ids estables para Postman, para k6, para Prometheus y ahora tambien para los backups locales de S22."

Mostrar:

- ejecucion de `schema.sql`
- ejecucion de `data.sql`
- una consulta corta a `students`, `sections`, `enrollments` o `billings`
- `powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1`

## 4. Salud y disponibilidad basica - 2:20 a 2:50

Guion sugerido:

"Cada servicio expone `GET /health` y, en el modo Compose, esos endpoints tambien alimentan el healthcheck del contenedor."

Mostrar:

- `curl.exe http://localhost:8081/health`
- `curl.exe http://localhost:8082/health`
- `curl.exe http://localhost:8083/health`
- `curl.exe http://localhost:8084/health`
- `curl.exe http://localhost:8085/health`

## 5. Flujo funcional con Postman - 2:50 a 4:20

Guion sugerido:

"Como no existe frontend, Postman sigue siendo el cliente operativo. El flujo critico sigue siendo la inscripcion de estudiante a una seccion y la generacion del cobro asociado."

Secuencia recomendada:

1. `00 - Health Checks`
2. `01 - Students`
3. `02 - Academic Catalog`
4. `03 - Enrollments`
5. `04 - Billings`
6. `05 - Notification`

Puntos a resaltar:

- `student-service` funciona como CRUD basico
- `course-service` expone cursos, periodos y secciones
- `POST /api/enrollments` crea la inscripcion
- repetir la misma inscripcion debe devolver `409 Conflict`
- repetir el mismo `POST /api/enrollments` con el mismo `Idempotency-Key` y el mismo body debe devolver el mismo `201` sin duplicar filas ni eventos
- esa misma inscripcion tambien debe crear una fila `outbox_events` para `EnrollmentCreatedEvent`
- `POST /api/billings` crea el cobro pendiente
- repetir el mismo cobro pendiente debe devolver `409 Conflict`
- repetir el mismo `POST /api/billings` con el mismo `Idempotency-Key` y el mismo body debe devolver el mismo `201` sin duplicar filas
- `PATCH /api/billings/{id}/status` a `CANCELLED` dispara el evento de cambio de estado
- ese cambio de estado debe crear una fila `outbox_events` para `BillingStatusChangedEvent`
- `enrollment-service` consume ese evento en `enrollment.compensation.events` y cambia la inscripcion relacionada a `CANCELLED`

## 6. Redis y RabbitMQ en vivo - 4:20 a 5:20

Guion sugerido:

"Redis y RabbitMQ ya no son teoria. `course-service` usa cache Redis. `enrollment-service` y `billing-service` primero escriben sus eventos al outbox transaccional en PostgreSQL y luego los publican a RabbitMQ. `notification` sigue consumiendo esos eventos para evidencia, y ahora `enrollment-service` tambien consume `billing.status.changed` para aplicar la compensacion basica."

Mostrar:

- `docker exec -i campusenroll-redis redis-cli ping`
- `docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"`
- `http://localhost:15672`
- una consulta a `outbox_events`
- logs de `notification`

## 7. k6, Prometheus y Grafana - 5:20 a 6:10

Guion sugerido:

"El repo tambien trae activos de validacion final. En S21, Prometheus ya scrapea metricas reales de los cinco servicios cuando el modo HA readiness esta activo. S28 agrega reglas activas de Prometheus para disponibilidad, target faltante, error HTTP y p95 de latencia. S29 agrega idempotencia real para retries HTTP en enrollments y billings. S30 agrega outbox transaccional para que la escritura de negocio y la creacion del evento queden persistidas atomicamente antes de RabbitMQ. S31 agrega una compensacion basica por choreografia que reutiliza ese mismo flujo confiable de publicacion. Grafana sigue disponible, pero no estamos reclamando un paquete de dashboards o notificaciones de nivel productivo."

Mostrar:

- `infra/k6/README.md`
- resumen de una ejecucion k6
- `curl.exe http://localhost:8081/actuator/prometheus`
- `http://localhost:9090/targets`
- `http://localhost:9090/alerts`
- `http://localhost:9090/rules`
- `http://localhost:3000`

## 8. Failover y switchover de aplicacion - 6:10 a 7:10

Guion sugerido:

"S25 no agrega failover de base de datos. Lo que si agrega es continuidad a nivel de aplicacion para course-service. HAProxy queda delante del catalogo academico y monitorea `course-service` y `course-service-replica` con `GET /health`. Si el primario cae o se saca por mantenimiento, el gateway sigue respondiendo."

Mostrar:

- `docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build`
- `curl.exe -i http://localhost:8080/api/courses`
- `Start-Process "http://localhost:8404/stats"`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml stop course-service`
- `curl.exe -i http://localhost:8080/api/courses`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml start course-service`

Puntos a remarcar:

- esto es failover/switchover de aplicacion, no failover de PostgreSQL
- ambos `course-service` usan la misma base PostgreSQL centralizada
- la continuidad se limita al catalogo academico en este segmento

## 9. Backup, restore y cierre honesto - 7:10 a 8:20

Guion sugerido:

"La evidencia final ya no solo incluye metricas y recuperacion de servicios. Tambien incluye backup manual de PostgreSQL, restore con advertencia visible y un runbook de recuperacion para perdida de datos, corrupcion del volumen o reconstruccion del entorno local. Y ahora queda claro que esa parte es la estrategia de recuperacion de datos, separada del failover de aplicacion que se mostro con HAProxy."

Mostrar:

- `powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1`
- `Get-ChildItem infra/backups/output`
- `powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force`
- `powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml stop course-service`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml start course-service`
- `curl.exe http://localhost:8082/health`
- opcional: `docker compose stop redis` y `curl.exe http://localhost:8082/api/courses`

Cierre sugerido:

"En conclusion, CampusEnroll HA ya es demostrable como plataforma local HA-ready: tiene empaquetado por servicio, restart policies, healthchecks, cache Redis, eventos RabbitMQ, outbox transaccional para los productores, una compensacion basica por choreografia para billings cancelados, metricas Prometheus reales, reglas activas de alerting en Prometheus, idempotencia HTTP real para escrituras criticas, activos de validacion y una capa local de backup/restore para PostgreSQL. Lo que sigue pendiente es la alta disponibilidad productiva con replicas, balanceo, un motor de saga completo, automatizacion de backups, almacenamiento off-site, cifrado, Alertmanager, dashboards de negocio, clusters y failover de base de datos."

Si preguntan por PostgreSQL failover, responder:

"No esta implementado. S25 cubre failover y switchover de la aplicacion course-service a traves de HAProxy. PostgreSQL sigue centralizado y la recuperacion actual del proyecto es backup y restore."
