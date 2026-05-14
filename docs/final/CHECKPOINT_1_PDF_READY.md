# Revision Tecnica Avanzada del Proyecto Final

Documento fuente PDF-ready para la entrega final de CampusEnroll HA.

## 1. Portada

- Curso y seccion: `[Completar]`
- Proyecto: CampusEnroll HA
- Segmento: S19
- Rol responsable: final validation, observability, demo, and delivery documentation owner
- Docente: `[Completar]`
- Integrantes: `[Completar]`
- Fecha: `[Completar]`
- URL del repositorio: `[Completar]`

[Insertar evidencia E01 - vista general del repositorio o portada final]

## 2. Resumen ejecutivo

CampusEnroll HA ya cuenta con una base funcional demostrable para estudiantes, catalogo academico, inscripciones y cobros. El proyecto tambien dispone de cache Redis para lecturas de catalogo, publicacion y consumo de eventos RabbitMQ orientados a evidencia, coleccion Postman para validacion manual, scripts k6 para pruebas finales y un entorno local de infraestructura compartida con PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.

Estado honesto del proyecto:

### Implementado actualmente

- `student-service` expone endpoints de estudiantes.
- `course-service` expone endpoints de catalogo y usa Redis como cache de lectura.
- `enrollment-service` expone endpoints de inscripcion y publica `EnrollmentCreatedEvent`.
- `billing-service` expone endpoints de cobro y publica `BillingStatusChangedEvent`.
- `notification` consume ambos eventos y registra evidencia en logs.
- `db/schema.sql` y `db/data.sql` permiten reiniciar el entorno demo.
- `postman/` es el cliente operativo actual para la demostracion.
- `infra/k6/` ya contiene pruebas de smoke, 50,000 requests, concurrencia y falla controlada.

### Configurado o preparado

- Docker Compose levanta PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- RabbitMQ Management UI queda disponible en `http://localhost:15672`.
- Prometheus queda disponible en `http://localhost:9090`.
- Grafana queda disponible en `http://localhost:3000`.

### Pendiente o mejora futura

- frontend
- gateway operativo como entrypoint real
- validacion cruzada automatica entre servicios
- generacion automatica de cobro a partir de la inscripcion
- scrapeo Prometheus de los microservicios y dashboards Grafana listos
- estrategia real de alta disponibilidad con replicas y failover

## 3. Flujo critico

Flujo critico de negocio para la entrega:

`Inscripcion de estudiante a una seccion y generacion de cobro asociado`

Forma actual de demostrarlo:

1. levantar infraestructura
2. cargar base de datos
3. ejecutar servicios localmente
4. crear inscripcion
5. evidenciar conflicto por duplicidad de inscripcion
6. crear cobro
7. evidenciar conflicto por cobro pendiente duplicado
8. cambiar el estado del cobro a `PAID`
9. observar Redis, RabbitMQ y logs

## 4. Arquitectura operativa actual

La arquitectura real de esta entrega no debe explicarse como una plataforma totalmente integrada de extremo a extremo. El cliente vigente es Postman y consume URLs directas por servicio. Docker Compose solo inicia infraestructura compartida. Los servicios Spring Boot se ejecutan fuera de Compose con `mvn spring-boot:run`.

Tabla de componentes:

| Componente | Estado actual | Comentario |
| --- | --- | --- |
| `student-service` | Implementado | CRUD basico y cambio de estado de estudiantes |
| `course-service` | Implementado | Catalogo academico con cache Redis |
| `enrollment-service` | Implementado | Inscripciones y publicacion de evento de creacion |
| `billing-service` | Implementado | Cobros y publicacion de evento de cambio de estado |
| `notification` | Implementado para evidencia | Consumidor de eventos y logs |
| `gateway-service` | Preparado, no operativo | No participa en la demo actual |
| PostgreSQL | Implementado en el flujo | Persistencia principal |
| Redis | Implementado en catalogo | Cache de lectura |
| RabbitMQ | Implementado para evidencia | Broker de eventos |
| Prometheus | Parcial | Infra disponible; no scrapea los microservicios |
| Grafana | Parcial | Infra disponible; sin dashboards provisionados en repo |

[Insertar evidencia E02 - infraestructura arriba]

## 5. Infraestructura y puertos

Valores relevantes del entorno local del repositorio:

| Recurso | Puerto local | Nota |
| --- | --- | --- |
| PostgreSQL | `55432` | definido en `.env` actual |
| Redis | `6379` | por defecto |
| RabbitMQ AMQP | `5672` | por defecto |
| RabbitMQ UI | `15672` | `guest/guest` |
| Prometheus | `9090` | UI local |
| Grafana | `3000` | `admin/admin` salvo cambio local |

Si se decide usar `5432` para PostgreSQL, los `*_DATASOURCE_URL` de los servicios deben apuntar a `jdbc:postgresql://localhost:5432/campusenroll`.

## 6. Base de datos y carga determinista

La base de datos se prepara con:

```powershell
docker compose up -d postgres
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Consulta minima de validacion:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_code, first_name, last_name, active FROM students ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, section_code, academic_period_id, course_id, capacity, active FROM sections ORDER BY id;"
```

El dataset demo deja una combinacion libre util para la demo: `studentId=1`, `sectionId=2`.

[Insertar evidencia E03 - carga de schema y seed]
[Insertar evidencia E04 - consulta de datos demo]

## 7. Ejecucion local de microservicios

Cada servicio se ejecuta en su propia terminal PowerShell.

### student-service

```powershell
Set-Location .\backend\student-service
$env:STUDENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### course-service

```powershell
Set-Location .\backend\course-service
$env:COURSE_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### enrollment-service

```powershell
Set-Location .\backend\enrollment-service
$env:ENROLLMENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### billing-service

```powershell
Set-Location .\backend\billing-service
$env:BILLING_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### notification

```powershell
Set-Location .\backend\notification
mvn spring-boot:run
```

## 8. Health endpoints

Verificacion:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Cada servicio debe responder con un JSON que contenga su nombre y `status=UP`.

[Insertar evidencia E05 - health checks]

## 9. Validacion funcional con Postman

Coleccion actual:

- `postman/campusenroll-ha.postman_collection.json`

Environment actual:

- `postman/campusenroll-ha.local.postman_environment.json`

Orden recomendado:

1. `00 - Health Checks`
2. `01 - Students`
3. `02 - Academic Catalog`
4. `03 - Enrollments`
5. `04 - Billings`
6. `05 - Notification`

Que debe comprobarse:

- estudiantes: consulta, creacion y cambio de estado
- catalogo: cursos, periodos y secciones
- inscripciones: alta valida y conflicto `409`
- cobros: alta valida y conflicto `409`
- notificacion: salud y, fuera de Postman, evidencia de eventos por logs

[Insertar evidencia E06 - Postman importado]
[Insertar evidencia E07 - respuesta de estudiantes]
[Insertar evidencia E08 - respuesta de catalogo]

## 10. Verificacion detallada del flujo critico

### Crear inscripcion

```powershell
$enrollment = Invoke-RestMethod -Method Post -Uri 'http://localhost:8083/api/enrollments' -ContentType 'application/json' -Body (@{
  studentId = 1
  sectionId = 2
} | ConvertTo-Json -Compress)
$enrollment
```

### Repetir la misma inscripcion para obtener `409`

```powershell
curl.exe -i -X POST http://localhost:8083/api/enrollments -H "Content-Type: application/json" -d '{"studentId":1,"sectionId":2}'
```

### Crear cobro pendiente

```powershell
$billing = Invoke-RestMethod -Method Post -Uri 'http://localhost:8084/api/billings' -ContentType 'application/json' -Body (@{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress)
$billing
```

### Repetir el mismo cobro pendiente para obtener `409`

```powershell
$duplicateBillingBody = @{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress
curl.exe -i -X POST http://localhost:8084/api/billings -H "Content-Type: application/json" -d $duplicateBillingBody
```

### Cambiar el cobro a `PAID`

```powershell
Invoke-RestMethod -Method Patch -Uri "http://localhost:8084/api/billings/$($billing.id)/status" -ContentType 'application/json' -Body (@{
  status = 'PAID'
} | ConvertTo-Json -Compress)
```

[Insertar evidencia E09 - inscripcion creada]
[Insertar evidencia E10 - conflicto de inscripcion]
[Insertar evidencia E11 - cobro creado]
[Insertar evidencia E12 - conflicto de cobro]

## 11. Verificacion de Redis cache

`course-service` ya utiliza Redis para las listas de catalogo.

Pasos:

```powershell
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

Lo que debe observarse:

- `PONG`
- respuestas correctas de `GET /api/courses`
- llaves `courses::*` visibles

[Insertar evidencia E13 - llaves Redis]

## 12. Verificacion de RabbitMQ y notification

RabbitMQ no debe presentarse como una idea futura: hoy ya interviene en el flujo de evidencia.

UI:

- `http://localhost:15672`
- usuario `guest`
- password `guest`

Inspeccion rapida:

```powershell
docker exec -i campusenroll-rabbitmq rabbitmqctl list_exchanges name type
docker exec -i campusenroll-rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key
docker exec -i campusenroll-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
```

Logs esperados:

- en `enrollment-service`:
  - `Published EnrollmentCreatedEvent ...`
- en `billing-service`:
  - `Published BillingStatusChangedEvent ...`
- en `notification`:
  - `Enrollment created event received ...`
  - `Billing status changed event received ...`

Nota importante:

- si la cola ya fue consumida, la mejor evidencia es combinar bindings en RabbitMQ con los logs de publicacion y consumo.

[Insertar evidencia E14 - RabbitMQ bindings]
[Insertar evidencia E15 - logs de notification]

## 13. Observabilidad actual

Prometheus y Grafana si forman parte del entorno local, pero la observabilidad todavia es parcial.

### Prometheus

```powershell
Start-Process 'http://localhost:9090/targets'
```

Estado actual:

- el target `prometheus` debe aparecer en `UP`
- no existe evidencia de scrapeo a los microservicios de negocio en la configuracion actual del repo

### Grafana

```powershell
Start-Process 'http://localhost:3000'
```

Estado actual:

- acceso con `admin/admin` salvo cambio local
- el repo no entrega datasource ni dashboards provisionados listos

[Insertar evidencia E19 - Prometheus targets]
[Insertar evidencia E20 - Grafana accesible]

## 14. Pruebas finales con k6

### Smoke

```powershell
k6 run .\infra\k6\smoke-test.js
```

### Exact 50,000 requests

```powershell
$env:REQUEST_TARGET='50000'
$env:VUS='100'
$env:MAX_DURATION='10m'
k6 run .\infra\k6\load-50000-requests.js
```

### Concurrencia de inscripcion

Para no interferir con la demo manual, usar `studentId=2` y `sectionId=1`:

```powershell
$env:ENROLLMENT_SERVICE_URL='http://localhost:8083'
$env:TEST_STUDENT_ID='2'
$env:TEST_SECTION_ID='1'
$env:VUS='20'
$env:ITERATIONS='20'
$env:MAX_DURATION='1m'
k6 run .\infra\k6\concurrent-enrollment-test.js
```

Metricas minimas a reportar:

- total requests
- `http_req_failed`
- `checks`
- p95
- p99
- throughput

[Insertar evidencia E16 - smoke k6]
[Insertar evidencia E17 - 50,000 requests]
[Insertar evidencia E18 - concurrencia k6]

## 15. Observacion de falla controlada

Escenario recomendado: detener Redis mientras `course-service` sigue arriba.

```powershell
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
docker compose stop redis
curl.exe http://localhost:8082/api/courses
docker compose start redis
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

La evidencia correcta debe incluir:

- contenedor Redis detenido
- `GET /api/courses` exitoso aun con Redis fuera
- warning de fallback en `course-service`
- Redis recuperado y llaves visibles otra vez

[Insertar evidencia E21 - Redis detenido y fallback]
[Insertar evidencia E22 - warning de course-service]
[Insertar evidencia E23 - Redis recuperado]

## 16. Pendientes y mejoras futuras

Los siguientes puntos deben quedar expresados como pendientes, no como trabajo ya completado:

- frontend
- gateway operativo
- validacion distribuida entre servicios antes de inscribir
- creacion automatica de cobro desde eventos
- scrapeo Prometheus de microservicios
- dashboards Grafana utiles para negocio y plataforma
- HA real con replicas, balanceo y recuperacion probada

## 17. Conclusiones

CampusEnroll HA ya puede presentarse como una base funcional con persistencia determinista, cache Redis de catalogo, publicacion y consumo de eventos RabbitMQ para evidencia, cliente Postman operativo, activos k6 listos para pruebas finales y stack local de infraestructura compartida. La entrega final debe sostener ese avance con pruebas visibles, pero sin exagerar la madurez actual de observabilidad, integracion automatica o alta disponibilidad.

[Insertar evidencia E24 - resumen final o cierre del PDF]
