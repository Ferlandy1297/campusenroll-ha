# Demo Script - Entrega Final S21

## Objetivo

Exponer en 5 a 8 minutos el estado real de CampusEnroll HA despues de S21, sin sobredeclarar cluster, failover u observabilidad de nivel productivo que el repo aun no entrega.

Mensaje central:

`CampusEnroll HA ya tiene flujo funcional, cache Redis, eventos RabbitMQ, healthchecks, modo Compose HA-ready y metricas Prometheus reales por microservicio; eso no equivale todavia a alta disponibilidad productiva.`

## 0. Preparacion previa

Antes de iniciar la demo:

- infraestructura arriba con `docker-compose.yml`
- `db/schema.sql` y `db/data.sql` cargados si el volumen es nuevo
- elegir una ruta para apps:
  - servicios con `mvn spring-boot:run`, o
  - servicios con `docker-compose.apps.yml`
- Postman importado
- RabbitMQ UI, Prometheus y Grafana accesibles
- una terminal PowerShell abierta en la raiz del repo

## 1. Apertura - 0:00 a 0:40

Guion sugerido:

"Este es CampusEnroll HA. La base actual ya permite demostrar estudiantes, catalogo, inscripciones y cobros. S20 agrego el modo Compose para levantar tambien los cinco microservicios Spring Boot con restart policy y healthchecks. S21 completa esa base con metricas Prometheus reales en los cinco servicios, sin romper el workflow Maven local."

Mostrar:

- `README.md`
- `docs/final/CHECKPOINT_1_PDF_READY.md`

## 2. Standard mode y HA readiness mode - 0:40 a 1:30

Guion sugerido:

"Ahora el repo tiene dos modos claros. El standard mode mantiene `docker-compose.yml` para infraestructura compartida. El HA readiness mode agrega `docker-compose.apps.yml` para contenedorizacion local de los cinco servicios."

Mostrar:

- `docker-compose.yml`
- `docker-compose.apps.yml`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml ps`

## 3. Base de datos determinista - 1:30 a 2:00

Guion sugerido:

"La base demo se reinicia con `db/schema.sql` y `db/data.sql`. Eso deja ids estables para Postman, para k6 y para la evidencia final."

Mostrar:

- ejecucion de `schema.sql`
- ejecucion de `data.sql`
- una consulta corta a `students`, `sections`, `enrollments` o `billings`

## 4. Salud y disponibilidad basica - 2:00 a 2:30

Guion sugerido:

"Cada servicio expone `GET /health` y, en el modo Compose, esos endpoints tambien alimentan el healthcheck del contenedor."

Mostrar:

- `curl.exe http://localhost:8081/health`
- `curl.exe http://localhost:8082/health`
- `curl.exe http://localhost:8083/health`
- `curl.exe http://localhost:8084/health`
- `curl.exe http://localhost:8085/health`

## 5. Flujo funcional con Postman - 2:30 a 4:20

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
- `POST /api/billings` crea el cobro pendiente
- repetir el mismo cobro pendiente debe devolver `409 Conflict`
- `PATCH /api/billings/{id}/status` a `PAID` dispara el evento de cambio de estado

## 6. Redis y RabbitMQ en vivo - 4:20 a 5:20

Guion sugerido:

"Redis y RabbitMQ ya no son teoria. `course-service` usa cache Redis. `enrollment-service` y `billing-service` publican eventos RabbitMQ, y `notification` los consume."

Mostrar:

- `docker exec -i campusenroll-redis redis-cli ping`
- `docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"`
- `http://localhost:15672`
- logs de `notification`

## 7. k6, Prometheus y Grafana - 5:20 a 6:20

Guion sugerido:

"El repo tambien trae activos de validacion final. En S21, Prometheus ya scrapea metricas reales de los cinco servicios cuando el modo HA readiness esta activo. Grafana sigue disponible, pero dashboards y alertas siguen pendientes."

Mostrar:

- `infra/k6/README.md`
- resumen de una ejecucion k6
- `curl.exe http://localhost:8081/actuator/prometheus`
- `http://localhost:9090/targets`
- `http://localhost:3000`

## 8. Recuperacion y cierre honesto - 6:20 a 7:20

Guion sugerido:

"La evidencia final de S21 incluye metricas Prometheus reales por servicio, detener y levantar `course-service` dentro del stack Compose, y tambien la degradacion controlada de Redis para mostrar recuperacion operativa basica."

Mostrar:

- `docker compose -f docker-compose.yml -f docker-compose.apps.yml stop course-service`
- `docker compose -f docker-compose.yml -f docker-compose.apps.yml start course-service`
- `curl.exe http://localhost:8082/health`
- opcional: `docker compose stop redis` y `curl.exe http://localhost:8082/api/courses`

Cierre sugerido:

"En conclusion, CampusEnroll HA ya es demostrable como plataforma local HA-ready: tiene empaquetado por servicio, restart policies, healthchecks, cache Redis, eventos RabbitMQ, metricas Prometheus reales y activos de validacion. Lo que sigue pendiente es la alta disponibilidad productiva con replicas, balanceo, alertas, dashboards, clusters y failover."
