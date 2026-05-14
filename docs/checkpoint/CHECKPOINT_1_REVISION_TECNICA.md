# Checkpoint 1 - Revision Tecnica Avanzada

## 1. Metadata del documento

- Proyecto: CampusEnroll HA
- Segmento: S19
- Rol responsable: final validation, observability, demo, and delivery documentation owner
- Fecha de alineacion: 2026-05-13

## 2. Resumen ejecutivo

### Implementado actualmente

- `student-service` expone `GET /health`, `GET /api/students`, `GET /api/students/{id}`, `POST /api/students` y `PATCH /api/students/{id}/status`.
- `course-service` expone `GET /health`, `GET/POST /api/courses`, `GET /api/courses/{id}`, `GET/POST /api/periods` y `GET/POST /api/sections`.
- `course-service` ya usa cache Redis para `GET /api/courses`, `GET /api/periods` y `GET /api/sections`.
- `enrollment-service` expone `GET /health`, `GET/POST /api/enrollments`, `GET /api/enrollments/{id}` y `PATCH /api/enrollments/{id}/status`.
- `enrollment-service` publica `EnrollmentCreatedEvent` al exchange `campusenroll.events` con routing key `enrollment.created`.
- `billing-service` expone `GET /health`, `GET/POST /api/billings`, `GET /api/billings/{id}` y `PATCH /api/billings/{id}/status`.
- `billing-service` publica `BillingStatusChangedEvent` al exchange `campusenroll.events` con routing key `billing.status.changed` cuando el estado cambia.
- `notification` consume `enrollment.created` y `billing.status.changed`, registra evidencia en memoria y escribe logs claros al recibir eventos.
- `docker-compose.yml` levanta PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana como infraestructura compartida.
- `db/schema.sql` y `db/data.sql` permiten reiniciar el estado demo de forma determinista.
- `postman/` ya contiene una coleccion local funcional para validacion directa por servicio.
- `infra/k6/` ya contiene smoke test, escenario exacto de 50,000 requests, concurrencia de inscripcion y guia de observacion de falla de contenedor.

### Configurado o preparado

- RabbitMQ Management UI queda disponible en `http://localhost:15672`.
- Prometheus queda disponible en `http://localhost:9090`.
- Grafana queda disponible en `http://localhost:3000`.
- La carpeta `backend/gateway-service/` permanece como referencia de arquitectura futura.

### Pendiente o mejora futura

- No existe frontend.
- Docker Compose no arranca los servicios Spring Boot.
- No hay validacion cruzada automatica entre inscripcion, estudiante y seccion.
- No existe generacion automatica de cobro desde el evento de inscripcion.
- Prometheus solo se scrapea a si mismo en la configuracion actual.
- El repo no trae datasource ni dashboards Grafana provisionados listos.
- No hay estrategia completa de HA con replicas, failover y recuperacion probada.

## 3. Flujo operativo actual

Flujo critico demostrable hoy:

`Inscripcion de estudiante a una seccion y generacion de cobro asociado`

Forma real de demostrarlo:

1. Levantar infraestructura con Docker Compose.
2. Cargar `db/schema.sql` y `db/data.sql`.
3. Ejecutar los cinco servicios Spring Boot localmente.
4. Validar salud con `GET /health`.
5. Usar Postman o comandos PowerShell para:
   - consultar estudiantes
   - consultar catalogo
   - crear una inscripcion
   - evidenciar conflicto por duplicidad de inscripcion activa
   - crear un cobro pendiente
   - evidenciar conflicto por cobro pendiente duplicado
   - cambiar el estado del cobro a `PAID`
6. Observar:
   - logs de publicacion en `enrollment-service` y `billing-service`
   - logs de consumo en `notification`
   - exchange, queue y bindings en RabbitMQ
   - llaves Redis despues de dos lecturas de catalogo

## 4. Infraestructura local

Variables y puertos relevantes del estado actual:

| Recurso | Valor actual del repo | Nota |
| --- | --- | --- |
| PostgreSQL host port | `55432` | Definido en `.env`; usar `5432` solo si se cambia localmente |
| Redis host port | `6379` | Igual al valor por defecto del servicio |
| RabbitMQ AMQP | `5672` | Igual al valor por defecto |
| RabbitMQ UI | `15672` | `guest/guest` |
| Prometheus | `9090` | UI local |
| Grafana | `3000` | `admin/admin` salvo cambio local |

## 5. Validacion final recomendada

### Preparacion

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

### Salud

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

### Redis

```powershell
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

### RabbitMQ

1. Crear una inscripcion valida.
2. Crear un cobro `PENDING` para esa inscripcion.
3. Cambiar el estado del cobro a `PAID`.
4. Observar:
   - `Published EnrollmentCreatedEvent ...`
   - `Published BillingStatusChangedEvent ...`
   - `Enrollment created event received ...`
   - `Billing status changed event received ...`
5. Confirmar queue y bindings desde la UI o con `rabbitmqctl`.

### k6

- `k6 run .\infra\k6\smoke-test.js`
- `k6 run .\infra\k6\load-50000-requests.js`
- `k6 run .\infra\k6\concurrent-enrollment-test.js`

Capturar siempre:

- total requests
- `http_req_failed`
- `checks`
- p95
- p99
- throughput

### Observabilidad

- Prometheus: abrir `http://localhost:9090/targets` y capturar el target `prometheus`.
- Grafana: abrir `http://localhost:3000`, autenticar y capturar el acceso actual.
- No afirmar dashboards de negocio ni scrapeo de servicios si no fueron configurados localmente.

### Falla controlada

Escenario recomendado:

1. Mantener `course-service` ejecutandose localmente.
2. Calentar cache con `GET /api/courses` dos veces.
3. Ejecutar `docker compose stop redis`.
4. Repetir `GET /api/courses`.
5. Capturar la continuidad de la respuesta y el warning de fallback en la consola del servicio.
6. Ejecutar `docker compose start redis` y repetir la verificacion de llaves.

## 6. Mensaje final honesto

CampusEnroll HA ya puede defenderse como una base funcional con persistencia, cache de catalogo, mensajeria de evidencia, cliente Postman, activos k6 y stack de infraestructura local. Lo que sigue pendiente no debe maquillarse:

- gateway operativo
- frontend
- observabilidad de aplicacion completa
- integracion automatica entre inscripcion y cobro
- alta disponibilidad real mas alla del entorno local
