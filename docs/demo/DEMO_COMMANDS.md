# Demo Commands - PowerShell - S32

## 0. Compilar y probar los servicios afectados por S31

```powershell
mvn -f backend/enrollment-service/pom.xml test
mvn -f backend/billing-service/pom.xml test
```

## 1. Confirmar variables locales

```powershell
Get-Content .\.env
```

Notas:

- El `.env` actual usa `POSTGRES_PORT=56432`.
- Si cambias a `5432`, las URLs JDBC del modo Maven deben usar ese mismo puerto.
- Frontend sigue fuera de alcance; Postman es el cliente operativo actual.
- El demo aislado S32 tambien usa `56432` para su PostgreSQL primary. No debe levantarse al mismo tiempo que `campusenroll-postgres` si ese contenedor ya esta ocupando el puerto.

## 2. HA readiness mode: levantar stack completo

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

Este es el comando recomendado para la defensa del estado actual.

## 3. Cargar PostgreSQL de forma determinista

Ejecutar este paso al menos la primera vez sobre un volumen PostgreSQL nuevo:

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
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d idempotency_records"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d outbox_events"
```

## 4. Verificar base y crear backup S22

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
Get-ChildItem infra/backups/output
```

Que debe observarse:

- conexion correcta a `campusenroll`
- usuario `campus`
- conteos visibles para `students`, `courses`, `sections`, `enrollments` y `billings`
- un archivo `.dump` nuevo dentro de `infra/backups/output`

## 5. Standard mode alternativo

Si el equipo quiere mostrar solo infraestructura compartida:

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

Este modo mantiene intacto el flujo local con `mvn spring-boot:run`.

## 6. Elegir modo de ejecucion de aplicaciones

### 6A. Modo Maven local

Abrir una terminal PowerShell por servicio.

#### Terminal 1 - student-service

```powershell
Set-Location .\backend\student-service
$env:STUDENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:56432/campusenroll'
mvn spring-boot:run
```

#### Terminal 2 - course-service

```powershell
Set-Location .\backend\course-service
$env:COURSE_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:56432/campusenroll'
mvn spring-boot:run
```

#### Terminal 3 - enrollment-service

```powershell
Set-Location .\backend\enrollment-service
$env:ENROLLMENT_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:56432/campusenroll'
mvn spring-boot:run
```

#### Terminal 4 - billing-service

```powershell
Set-Location .\backend\billing-service
$env:BILLING_SERVICE_DATASOURCE_URL='jdbc:postgresql://localhost:56432/campusenroll'
mvn spring-boot:run
```

#### Terminal 5 - notification

```powershell
Set-Location .\backend\notification
mvn spring-boot:run
```

### 6B. HA readiness mode con contenedores

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

Este modo agrega:

- build por servicio desde `backend/<service>`
- `restart: unless-stopped`
- healthcheck contra `GET /health`
- hostnames internos `postgres`, `redis` y `rabbitmq`

Mensaje honesto:

- este modo es demostrable para readiness local
- no es alta disponibilidad productiva ni cluster real

## 7. Verificar salud

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Cada respuesta debe incluir `status=UP` y el nombre del servicio.

## 8. Verificar contenedores y healthchecks del modo HA readiness

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
docker compose ps postgres redis rabbitmq prometheus grafana
```

Si estas en modo HA readiness, la tabla debe mostrar:

- infraestructura arriba
- `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` arriba
- estado `healthy` cuando el healthcheck haya completado

## 9. Evidencia de recuperacion por reinicio de servicio

Escenario recomendado para la continuidad operativa local:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml stop course-service
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
docker compose -f docker-compose.yml -f docker-compose.apps.yml start course-service
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
curl.exe http://localhost:8082/health
```

Que debe capturarse:

- el servicio detenido
- el reinicio manual exitoso
- `GET /health` respondiendo de nuevo

## 10. Validar Postman

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

## 11. Flujo funcional exacto para la demo

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

### Cambiar el estado del cobro a `CANCELLED`

```powershell
$cancelledBilling = Invoke-RestMethod -Method Patch -Uri "http://localhost:8084/api/billings/$($billing.id)/status" -ContentType 'application/json' -Body (@{
  status = 'CANCELLED'
} | ConvertTo-Json -Compress)
$cancelledBilling
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, status FROM billings WHERE id = $($billing.id);"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments WHERE id = $($enrollment.id);"
```

Resultado esperado:

- el billing queda en `CANCELLED`
- la inscripcion relacionada pasa de `ENROLLED` a `CANCELLED`
- si vuelves a ejecutar el mismo `PATCH` con `CANCELLED`, el billing queda igual y la compensacion no rompe el estado ya cancelado

### Validar `Idempotency-Key` en enrollments

Usar un par libre despues de recargar `db/schema.sql` y `db/data.sql`. El dataset actual deja libre `studentId=1`, `sectionId=2`.

```powershell
$enrollmentHeaders = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "enrollment-idem-demo-1"
}
$enrollmentBody = '{"studentId":1,"sectionId":2}'
$firstEnrollment = Invoke-WebRequest -Method Post -Uri 'http://localhost:8083/api/enrollments' -Headers $enrollmentHeaders -Body $enrollmentBody
$secondEnrollment = Invoke-WebRequest -Method Post -Uri 'http://localhost:8083/api/enrollments' -Headers $enrollmentHeaders -Body $enrollmentBody
$firstEnrollment.StatusCode
$secondEnrollment.StatusCode
$firstEnrollment.Content
$secondEnrollment.Content
$differentEnrollmentBody = (@{
  studentId = 1
  sectionId = 3
} | ConvertTo-Json -Compress)
curl.exe -i -X POST http://localhost:8083/api/enrollments -H "Content-Type: application/json" -H "Idempotency-Key: enrollment-idem-demo-1" -d $differentEnrollmentBody
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments WHERE student_id = 1 AND section_id = 2 ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT service_name, operation_name, idempotency_key, status, response_status FROM idempotency_records WHERE service_name = 'enrollment-service' ORDER BY id;"
```

Resultado esperado:

- primer POST: `201 Created`
- segundo POST con mismo header y mismo body: `201 Created` con el mismo JSON
- mismo key con body distinto: `409 Conflict`
- una sola fila nueva en `enrollments` para `student_id=1` y `section_id=2`

### Validar `Idempotency-Key` en billings

```powershell
$enrollmentReplay = $firstEnrollment.Content | ConvertFrom-Json
$billingHeaders = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "billing-idem-demo-1"
}
$billingBody = (@{
  enrollmentId = $enrollmentReplay.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress)
$firstBilling = Invoke-WebRequest -Method Post -Uri 'http://localhost:8084/api/billings' -Headers $billingHeaders -Body $billingBody
$secondBilling = Invoke-WebRequest -Method Post -Uri 'http://localhost:8084/api/billings' -Headers $billingHeaders -Body $billingBody
$firstBilling.StatusCode
$secondBilling.StatusCode
$firstBilling.Content
$secondBilling.Content
$differentBillingBody = (@{
  enrollmentId = $enrollmentReplay.id
  amount = 175.00
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress)
curl.exe -i -X POST http://localhost:8084/api/billings -H "Content-Type: application/json" -H "Idempotency-Key: billing-idem-demo-1" -d $differentBillingBody
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, amount, currency, status FROM billings WHERE enrollment_id = $($enrollmentReplay.id) ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT service_name, operation_name, idempotency_key, status, response_status FROM idempotency_records WHERE service_name = 'billing-service' ORDER BY id;"
```

Resultado esperado:

- primer POST: `201 Created`
- segundo POST con mismo header y mismo body: `201 Created` con el mismo JSON
- mismo key con body distinto: `409 Conflict`
- una sola fila nueva en `billings` para el `enrollmentId` usado

## 12. Verificar Redis cache

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

## 13. Verificar outbox transaccional S30 y compensacion basica S31

Despues de crear la inscripcion y cambiar el estado del cobro a `CANCELLED`, inspeccionar el outbox:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, event_type, routing_key, status, attempts, created_at, published_at FROM outbox_events ORDER BY id DESC LIMIT 20;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, aggregate_type, aggregate_id, event_type, status, attempts, last_error FROM outbox_events WHERE aggregate_type = 'enrollment' AND aggregate_id = $($enrollment.id) ORDER BY id DESC;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, aggregate_type, aggregate_id, event_type, status, attempts, last_error FROM outbox_events WHERE aggregate_type = 'billing' AND aggregate_id = $($billing.id) ORDER BY id DESC;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments ORDER BY id DESC LIMIT 10;"
```

Resultado esperado:

- una fila de `enrollment-service` para `EnrollmentCreatedEvent`
- una fila de `billing-service` para `BillingStatusChangedEvent`
- estado normalmente `PUBLISHED` pocos segundos despues de la escritura
- `published_at` no nulo cuando RabbitMQ ya recibio el evento
- `attempts=0` en un escenario sano
- la fila de `enrollments` asociada al billing cancelado debe verse como `CANCELLED`

## 14. Verificar RabbitMQ y logs de eventos

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

Log lines que deben observarse:

- en `enrollment-service`:
  - `Processed billing cancellation compensation ...`
- en `enrollment-service`:
  - `Published EnrollmentCreatedEvent ...`
- en `billing-service`:
  - `Published BillingStatusChangedEvent ...`
- en `notification`:
  - `Enrollment created event received ...`
  - `Billing status changed event received ...`

Filas y bindings que deben observarse:

- una cola `enrollment.compensation.events`
- un binding desde `campusenroll.events` hacia `enrollment.compensation.events` con routing key `billing.status.changed`
- la cola `notification.events` debe seguir enlazada y consumiendo sin romperse

## 15. Ejecutar k6

### Smoke test

```powershell
k6 run .\infra\k6\smoke-test.js
```

### Smoke test con Docker si `k6` no esta instalado

Evitar en PowerShell el patron `docker run ... run - < archivo.js`. Usar volumen montado y `host.docker.internal`:

```powershell
docker run --rm -i `
  -e STUDENT_SERVICE_URL=http://host.docker.internal:8081 `
  -e COURSE_SERVICE_URL=http://host.docker.internal:8082 `
  -e ENROLLMENT_SERVICE_URL=http://host.docker.internal:8083 `
  -e BILLING_SERVICE_URL=http://host.docker.internal:8084 `
  -e NOTIFICATION_SERVICE_URL=http://host.docker.internal:8085 `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

Si prefieres ejecutar k6 dentro de la red Compose, agregar `--network <compose-network>` y cambiar `host.docker.internal` por los nombres de servicio Docker.

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

## 16. Verificar Prometheus, alertas, metricas y Grafana

Si Prometheus ya estaba arriba antes de actualizar `infra/prometheus/prometheus.yml`, reiniciarlo una vez para forzar la recarga:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml restart prometheus
```

```powershell
curl.exe http://localhost:8081/actuator/prometheus
curl.exe http://localhost:8082/actuator/prometheus
curl.exe http://localhost:8083/actuator/prometheus
curl.exe http://localhost:8084/actuator/prometheus
curl.exe http://localhost:8085/actuator/prometheus
docker compose ps prometheus grafana
Start-Process 'http://localhost:9090/targets'
Start-Process 'http://localhost:9090/alerts'
Start-Process 'http://localhost:9090/rules'
Start-Process 'http://localhost:3000'
```

Comandos de validacion S28:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build prometheus
Start-Sleep -Seconds 20
curl.exe http://localhost:9090/-/ready
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml restart prometheus
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml logs prometheus --tail=120
```

Capturas recomendadas:

- respuestas no vacias de los cinco endpoints `/actuator/prometheus`
- Prometheus con `prometheus`, `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` en estado `UP`
- UI de `Alerts` mostrando las reglas cargadas
- UI de `Rules` mostrando los grupos `campusenroll-availability` y `campusenroll-http`
- Grafana accesible y respondiendo

Mensaje honesto:

- S21 agrego metricas reales de microservicios mediante Actuator y Micrometer Prometheus
- S28 agrega reglas activas de Prometheus para disponibilidad, target faltante, 5xx y p95 de latencia
- S29 agrega `Idempotency-Key` real para `POST /api/enrollments` y `POST /api/billings`
- S30 agrega outbox transaccional para `EnrollmentCreatedEvent` y `BillingStatusChangedEvent`
- S31 agrega compensacion basica por choreografia: cuando un billing pasa a `CANCELLED`, `enrollment-service` consume `billing.status.changed` y cancela la inscripcion relacionada
- el workflow Maven local sigue intacto, pero los targets por nombre de servicio Docker solo apareceran `UP` en la UI de Prometheus cuando `docker-compose.apps.yml` este activo
- si Prometheus ya venia ejecutandose desde una corrida anterior, puede requerir un `restart prometheus` para recargar la nueva configuracion montada
- Alertmanager y notificaciones externas siguen fuera del alcance actual
- Grafana sigue disponible, pero dashboards de negocio, replicas y clustering siguen como mejora futura
- esto no implementa orquestacion central ni un motor de saga completo

## 17. Observacion de falla controlada de infraestructura

Escenario recomendado: degradacion de Redis con `course-service` arriba.

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

Ademas, mantener visible la consola o logs de `course-service` para capturar el fallback de cache.

## 18. Restore de backup con advertencia visible

Usar este paso solo como prueba manual controlada. El restore sobrescribe objetos actuales de la base.

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

Recomendacion operativa:

- detener escrituras de aplicaciones antes del restore
- usar un backup recien creado o un dump conocido como valido
- capturar la advertencia de sobrescritura y la verificacion posterior

Mensaje honesto de S22:

- ahora existe una estrategia local de backup y restore para PostgreSQL
- esto fortalece la continuidad operativa y la defensa academica del proyecto
- produccion seguiria necesitando automatizacion, almacenamiento off-site, cifrado y politicas de retencion probadas

## 19. S25 - failover y switchover a nivel de aplicacion con HAProxy

Levantar el modo demo:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

Verificacion inicial:

```powershell
curl.exe -i http://localhost:8080/api/courses
curl.exe -i http://localhost:8080/health/course
Start-Process "http://localhost:8404/stats"
```

Failover por caida del primario:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml stop course-service
curl.exe -i http://localhost:8080/api/courses
```

Resultado esperado:

- el gateway sigue devolviendo `HTTP 200`
- la continuidad la mantiene `course-service-replica`
- HAProxy detecta la caida del primario porque su `GET /health` deja de responder

Recuperar el primario:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml start course-service
curl.exe -i http://localhost:8080/api/courses
```

Switchover planeado:

- usar el mismo `stop course-service` como salida controlada por mantenimiento
- verificar que el gateway sigue respondiendo
- reiniciar el primario y dejarlo volver al pool

Atajos con script:

```powershell
powershell -ExecutionPolicy Bypass -File infra/ha/failover-demo.ps1 -OpenStats
powershell -ExecutionPolicy Bypass -File infra/ha/switchover-demo.ps1 -OpenStats
```

Mensaje exacto para la defensa:

- esto si es failover y switchover a nivel de aplicacion para `course-service`
- esto no es failover de PostgreSQL
- PostgreSQL sigue centralizado y su recuperacion actual se resuelve con backup y restore

## 20. S32 - demo aislado de replicacion PostgreSQL y failover manual

Antes de iniciar el demo S32, liberar `56432` si `campusenroll-postgres` ya esta arriba:

```powershell
docker stop campusenroll-postgres
```

Validar Compose:

```powershell
docker compose -f docker-compose.db-ha-demo.yml config
```

Levantar el demo:

```powershell
docker compose -f docker-compose.db-ha-demo.yml up -d --build
docker compose -f docker-compose.db-ha-demo.yml ps
```

Verificar primary y replica:

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT application_name, state, sync_state FROM pg_stat_replication;"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT status, conninfo FROM pg_stat_wal_receiver;"
```

Resultado esperado:

- primary: `pg_is_in_recovery() = false`
- replica: `pg_is_in_recovery() = true`
- `pg_stat_replication` muestra una fila de replica normalmente en `streaming`
- `pg_stat_wal_receiver` muestra `status = streaming`

Probar replicacion visible:

```powershell
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('replicated-from-primary');"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
```

Resultado esperado:

- la fila `replicated-from-primary` aparece en la replica

Demostrar que la replica es solo lectura antes de promocion:

```powershell
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('should-fail-on-replica');"
```

Resultado esperado:

- error porque la replica sigue en recovery

Failover manual:

```powershell
docker stop campusenroll-pg-primary
docker exec -u postgres campusenroll-pg-replica pg_ctl -D /var/lib/postgresql/data promote
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('written-after-promotion'); SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
```

Resultado esperado:

- `pg_is_in_recovery() = false` despues de la promocion
- la replica promovida acepta escrituras

Reset:

```powershell
docker compose -f docker-compose.db-ha-demo.yml down -v
```

Mensaje exacto para la defensa:

- S32 si implementa un demo aislado de PostgreSQL primary y read replica con streaming replication
- el failover aqui es manual y se demuestra promoviendo la replica
- esto no reemplaza el PostgreSQL principal del stack de aplicacion
- esto no es failover automatico ni un cluster Patroni, repmgr o pg_auto_failover
