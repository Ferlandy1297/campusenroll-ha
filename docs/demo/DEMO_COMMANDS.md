# Demo Commands - PowerShell

## 1. Confirmar variables locales

```powershell
Get-Content .\.env
```

Notas:

- El `.env` actual usa `POSTGRES_PORT=55432`.
- Si cambias a `5432`, los `*_DATASOURCE_URL` de los servicios deben usar ese mismo puerto.

## 2. Levantar infraestructura compartida

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

## 3. Cargar PostgreSQL de forma determinista

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\dt"
```

Consultas utiles para evidencia:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_code, first_name, last_name, active FROM students ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, section_code, academic_period_id, course_id, capacity, active FROM sections ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, amount, currency, status FROM billings ORDER BY id;"
```

## 4. Ejecutar los microservicios Spring Boot

Abrir una terminal PowerShell por servicio.

### Terminal 1 - student-service

```powershell
Set-Location .\backend\student-service
$env:STUDENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### Terminal 2 - course-service

```powershell
Set-Location .\backend\course-service
$env:COURSE_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### Terminal 3 - enrollment-service

```powershell
Set-Location .\backend\enrollment-service
$env:ENROLLMENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### Terminal 4 - billing-service

```powershell
Set-Location .\backend\billing-service
$env:BILLING_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:55432/campusenroll'
mvn spring-boot:run
```

### Terminal 5 - notification

```powershell
Set-Location .\backend\notification
mvn spring-boot:run
```

Si tu PostgreSQL local esta en `5432`, reemplaza `55432` por `5432` en las cuatro variables JDBC.

## 5. Verificar salud

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Cada respuesta debe incluir `status=UP` y el nombre del servicio.

## 6. Validar Postman

Pasos manuales:

1. Importar `postman/campusenroll-ha.postman_collection.json`.
2. Importar `postman/campusenroll-ha.local.postman_environment.json`.
3. Ejecutar en este orden:
   - `00 - Health Checks`
   - `01 - Students`
   - `02 - Academic Catalog`
   - `03 - Enrollments`
   - `04 - Billings`
   - `05 - Notification`

## 7. Flujo funcional exacto para la demo

El dataset demo deja libre la combinacion `studentId=1` y `sectionId=2`.

### Crear una inscripcion valida

```powershell
$enrollment = Invoke-RestMethod -Method Post -Uri 'http://localhost:8083/api/enrollments' -ContentType 'application/json' -Body (@{
  studentId = 1
  sectionId = 2
} | ConvertTo-Json -Compress)
$enrollment
```

### Evidenciar conflicto por duplicidad de inscripcion activa

```powershell
curl.exe -i -X POST http://localhost:8083/api/enrollments -H "Content-Type: application/json" -d '{"studentId":1,"sectionId":2}'
```

### Crear un cobro pendiente para esa inscripcion

```powershell
$billing = Invoke-RestMethod -Method Post -Uri 'http://localhost:8084/api/billings' -ContentType 'application/json' -Body (@{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress)
$billing
```

### Evidenciar conflicto por cobro pendiente duplicado

```powershell
$duplicateBillingBody = @{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress
curl.exe -i -X POST http://localhost:8084/api/billings -H "Content-Type: application/json" -d $duplicateBillingBody
```

### Cambiar el estado del cobro a `PAID`

```powershell
Invoke-RestMethod -Method Patch -Uri "http://localhost:8084/api/billings/$($billing.id)/status" -ContentType 'application/json' -Body (@{
  status = 'PAID'
} | ConvertTo-Json -Compress)
```

## 8. Verificar Redis cache

```powershell
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

Evidencia esperada:

- `PONG`
- respuestas correctas de `GET /api/courses`
- al menos una llave `courses::*`

## 9. Verificar RabbitMQ y logs de eventos

Abrir la UI:

```powershell
Start-Process 'http://localhost:15672'
```

Credenciales esperadas:

- usuario: `guest`
- password: `guest`

Inspeccion rapida por terminal:

```powershell
docker exec -i campusenroll-rabbitmq rabbitmqctl list_exchanges name type
docker exec -i campusenroll-rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key
docker exec -i campusenroll-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
```

Log lines que deben observarse en las terminales de servicios:

- en `enrollment-service`:
  - `Published EnrollmentCreatedEvent ...`
- en `billing-service`:
  - `Published BillingStatusChangedEvent ...`
- en `notification`:
  - `Enrollment created event received ...`
  - `Billing status changed event received ...`

Nota:

- la cola `notification.events` puede drenarse rapido porque `notification` esta consumiendo activamente; la evidencia principal debe combinar UI o `rabbitmqctl` con logs de publicacion y consumo.

## 10. Ejecutar k6

### Smoke test

```powershell
k6 run .\infra\k6\smoke-test.js
```

### Escenario exacto de 50,000 requests

```powershell
$env:REQUEST_TARGET='50000'
$env:VUS='100'
$env:MAX_DURATION='10m'
k6 run .\infra\k6\load-50000-requests.js
```

### Concurrencia sobre inscripcion

Usar un par distinto al de la demo manual. El dataset deja libre `studentId=2`, `sectionId=1`.

```powershell
$env:ENROLLMENT_SERVICE_URL='http://localhost:8083'
$env:TEST_STUDENT_ID='2'
$env:TEST_SECTION_ID='1'
$env:VUS='20'
$env:ITERATIONS='20'
$env:MAX_DURATION='1m'
k6 run .\infra\k6\concurrent-enrollment-test.js
```

Capturar siempre del resumen final:

- total requests
- `http_req_failed`
- `checks`
- p95
- p99
- throughput

Si `k6` no esta instalado localmente, usar el fallback Docker documentado en `infra/k6/README.md`.

## 11. Verificar Prometheus y Grafana

```powershell
docker compose ps prometheus grafana
Start-Process 'http://localhost:9090/targets'
Start-Process 'http://localhost:3000'
```

Capturas recomendadas:

- Prometheus con el target `prometheus` en estado `UP`
- Grafana con acceso exitoso a la UI

Mensaje honesto:

- el repo no provisiona datasource ni dashboards listos
- Prometheus no scrapea aun a los microservicios de negocio

## 12. Observacion de falla controlada

Escenario recomendado: degradacion de Redis con `course-service` en ejecucion.

```powershell
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
docker compose stop redis
curl.exe http://localhost:8082/api/courses
docker compose logs redis --tail 50
docker compose start redis
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

Ademas, mantener visible la consola de `course-service` para capturar el warning de fallback de cache.
