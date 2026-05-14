# Demo Script - Entrega Final S19

## Objetivo

Exponer en 5 a 8 minutos el estado real de CampusEnroll HA despues de S17 y S18, sin sobredeclarar observabilidad ni automatizacion que el repo aun no entrega por completo.

Mensaje central:

`CampusEnroll HA ya tiene flujo funcional, cache Redis en catalogo, eventos RabbitMQ de evidencia y activos reales de validacion; la integracion completa y la observabilidad de aplicacion aun son parciales.`

## 0. Preparacion previa

Antes de iniciar la demo:

- infraestructura levantada con Docker Compose
- `db/schema.sql` y `db/data.sql` cargados
- cinco microservicios ejecutandose con `mvn spring-boot:run`
- Postman importado
- RabbitMQ UI, Prometheus y Grafana accesibles
- una terminal PowerShell abierta en la raiz del repo

## 1. Apertura - 0:00 a 0:40

Guion sugerido:

"Este es CampusEnroll HA. La base actual ya permite demostrar estudiantes, catalogo, inscripciones y cobros. Ademas, `course-service` ya usa Redis para cache de lectura, `enrollment-service` y `billing-service` ya publican eventos en RabbitMQ, y `notification` ya consume esos eventos como evidencia."

Mostrar:

- `README.md`
- `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md`

## 2. Infraestructura y alcance real - 0:40 a 1:20

Guion sugerido:

"Docker Compose levanta solo la infraestructura compartida: PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana. Los servicios Spring Boot se ejecutan localmente. No hay frontend y el gateway no es el entrypoint operativo del flujo actual."

Mostrar:

- `docker compose ps`
- `docker-compose.yml`

## 3. Base de datos determinista - 1:20 a 1:50

Guion sugerido:

"La base demo se reinicia con `db/schema.sql` y `db/data.sql`. Eso deja ids estables para la validacion funcional y para las pruebas de concurrencia."

Mostrar:

- `db/schema.sql`
- `db/data.sql`
- una consulta corta a `students`, `sections`, `enrollments` o `billings`

## 4. Salud y endpoints base - 1:50 a 2:20

Guion sugerido:

"Cada servicio expone `GET /health` con su nombre y `status=UP`. Esta es la verificacion basica antes de pasar a Postman."

Mostrar:

- `curl.exe http://localhost:8081/health`
- `curl.exe http://localhost:8082/health`
- `curl.exe http://localhost:8083/health`
- `curl.exe http://localhost:8084/health`
- `curl.exe http://localhost:8085/health`

## 5. Flujo funcional con Postman - 2:20 a 4:20

Guion sugerido:

"Como no existe frontend, Postman es el cliente operativo actual. El flujo critico sigue siendo la inscripcion de estudiante a una seccion y la generacion del cobro asociado."

Secuencia recomendada:

1. `00 - Health Checks`
2. `01 - Students`
3. `02 - Academic Catalog`
4. `03 - Enrollments`
5. `04 - Billings`
6. `05 - Notification`

Puntos a resaltar:

- `student-service` funciona como CRUD basico con cambio de estado.
- `course-service` expone cursos, periodos y secciones.
- `POST /api/enrollments` crea la inscripcion.
- repetir `POST /api/enrollments` sobre el mismo par debe devolver `409 Conflict`.
- `POST /api/billings` crea el cobro pendiente.
- repetir `POST /api/billings` sobre la misma inscripcion pendiente debe devolver `409 Conflict`.
- `PATCH /api/billings/{id}/status` a `PAID` dispara el evento de cambio de estado.

## 6. Redis cache en vivo - 4:20 a 4:50

Guion sugerido:

"La cache ya no es teorica. `course-service` usa Redis para listas de catalogo. La forma simple de probarlo es consultar `GET /api/courses` dos veces y luego listar llaves `courses::*`."

Mostrar:

- `docker exec -i campusenroll-redis redis-cli ping`
- `curl.exe http://localhost:8082/api/courses`
- `curl.exe http://localhost:8082/api/courses`
- `docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"`

## 7. RabbitMQ y notification - 4:50 a 5:40

Guion sugerido:

"RabbitMQ tambien participa hoy. `enrollment-service` publica `EnrollmentCreatedEvent`, `billing-service` publica `BillingStatusChangedEvent`, y `notification` consume ambos para dejar evidencia en logs."

Mostrar:

- `http://localhost:15672`
- `rabbitmqctl list_exchanges`
- `rabbitmqctl list_bindings`
- terminal de `notification` con los logs:
  - `Enrollment created event received ...`
  - `Billing status changed event received ...`

Mensaje importante:

- la cola puede vaciarse rapido porque el consumidor esta activo
- por eso la evidencia correcta combina UI o `rabbitmqctl` con logs de publicacion y consumo

## 8. k6 y observabilidad - 5:40 a 6:30

Guion sugerido:

"El repo ya contiene activos de prueba final: smoke, 50,000 requests, concurrencia de inscripcion y observacion de falla de contenedor. Tambien ya levanta Prometheus y Grafana, aunque todavia sin metricas de aplicacion scrapeadas ni dashboards provisionados."

Mostrar:

- `infra/k6/README.md`
- `infra/k6/smoke-test.js`
- `infra/k6/load-50000-requests.js`
- `infra/k6/concurrent-enrollment-test.js`
- `http://localhost:9090/targets`
- `http://localhost:3000`

Resaltar:

- del resumen k6 siempre interesa `total requests`, `http_req_failed`, `checks`, p95, p99 y throughput
- Prometheus hoy solo se scrapea a si mismo
- Grafana hoy prueba acceso a la UI, no dashboards del negocio

## 9. Falla controlada - 6:30 a 7:00

Guion sugerido:

"El escenario de falla recomendado es detener Redis y comprobar que `course-service` sigue respondiendo `GET /api/courses` con fallback a base de datos."

Mostrar:

- `docker compose stop redis`
- `curl.exe http://localhost:8082/api/courses`
- warning de fallback en la consola de `course-service`
- `docker compose start redis`

## 10. Cierre - 7:00 a 7:20

Guion sugerido:

"En conclusion, CampusEnroll HA ya puede demostrar funcionalidad real, cache Redis, eventos RabbitMQ y un paquete serio de validacion final. Lo pendiente sigue siendo la integracion automatica completa entre servicios, la observabilidad de aplicacion y la alta disponibilidad real."

## Version corta de 5 minutos

Si el tiempo baja:

1. mostrar `docker compose ps`
2. mostrar `GET /health`
3. ejecutar Postman para inscripcion y cobro
4. mostrar Redis con `courses::*`
5. mostrar logs de `notification`
6. cerrar con pendientes honestos
