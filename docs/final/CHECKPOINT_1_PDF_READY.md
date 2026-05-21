# Revision Tecnica Avanzada del Proyecto Final

Documento fuente PDF-ready para la entrega final de CampusEnroll HA.

## 1. Portada

- Curso y seccion: `[Completar]`
- Proyecto: CampusEnroll HA
- Segmento: S22
- Rol responsable: backup, restore, and disaster recovery owner
- Docente: `[Completar]`
- Integrantes: `[Completar]`
- Fecha: `[Completar]`
- URL del repositorio: `[Completar]`

[Insertar evidencia E01 - vista general del repositorio]

## 2. Resumen ejecutivo

CampusEnroll HA ya cuenta con una base funcional demostrable para estudiantes, catalogo academico, inscripciones y cobros. S20 agrego una capa segura de readiness local con Docker Compose para los cinco microservicios Spring Boot. S21 completo ese avance al exponer metricas Actuator/Prometheus reales en los cinco servicios y al dejar a Prometheus scrapeando esos endpoints dentro del modo HA readiness. S22 agrega una capa practica de backup, restore y recuperacion ante desastres para PostgreSQL.

Mensaje central:

`CampusEnroll HA ya es demostrable como plataforma local HA-ready con recuperacion de datos PostgreSQL, pero no debe presentarse como una solucion de alta disponibilidad productiva.`

### Implementado actualmente

- `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` tienen Dockerfile operativo.
- `docker-compose.yml` mantiene infraestructura compartida para PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- `docker-compose.apps.yml` agrega los cinco servicios de aplicacion en puertos `8081` a `8085`.
- Infraestructura y aplicaciones usan `restart: unless-stopped`.
- Infraestructura y aplicaciones exponen healthchecks verificables.
- Los cinco servicios exponen `GET /actuator/health`, `GET /actuator/info` y `GET /actuator/prometheus`.
- Prometheus scrapea `prometheus`, `student-service`, `course-service`, `enrollment-service`, `billing-service` y `notification` en modo HA readiness.
- `course-service` usa Redis como cache real.
- `enrollment-service` y `billing-service` publican eventos RabbitMQ.
- `notification` consume esos eventos y deja evidencia en logs.
- `infra/backups/backup-postgres.ps1` crea dumps locales de PostgreSQL.
- `infra/backups/restore-postgres.ps1` restaura un dump seleccionado con advertencia visible.
- `infra/backups/verify-database.ps1` valida base, usuario y conteos clave.
- `infra/backups/DISASTER_RECOVERY_RUNBOOK.md` documenta perdida de datos, corrupcion de volumen y reconstruccion del entorno local.
- `postman/` sigue siendo el cliente operativo actual.
- `infra/k6/` sigue siendo el paquete de validacion final.

### Configurado o preparado

- Grafana esta disponible como infraestructura local accesible.
- Compose ya permite demostrar arranque coordinado, estado de salud, metricas Prometheus, backup PostgreSQL y recuperacion manual de servicios.

### Pendiente o mejora futura

- replicas multiples por servicio
- balanceador real
- cluster Redis
- cluster RabbitMQ
- replicacion y failover de PostgreSQL
- backups programados
- almacenamiento off-site
- cifrado de backups
- retencion automatizada
- Kubernetes o Docker Swarm
- dashboards Grafana listos para plataforma y negocio
- alertas Prometheus/Grafana

## 3. Objetivo tecnico de S22

El objetivo de este segmento no fue rehacer la arquitectura ni reemplazar el flujo Maven existente. El objetivo fue completar la continuidad operativa local con cambios pequenos y revisables:

1. conservar `docker-compose.yml` como modo estandar de infraestructura
2. conservar `docker-compose.apps.yml` como modo Compose adicional para aplicaciones
3. mantener operativos los endpoints custom `GET /health`
4. agregar una capa local de backup PostgreSQL con `pg_dump -Fc`
5. agregar restore protegido con `-Force` y `pg_restore --clean --if-exists`
6. dejar una verificacion simple de base y un runbook de recuperacion ante desastres
7. mantener explicitos los limites entre recuperacion local academica y controles productivos reales

## 4. Arquitectura operativa actual

La entrega final debe describirse con dos modos de ejecucion, no con una sola narrativa.

### Standard mode

- `docker-compose.yml` levanta solo PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana
- los microservicios pueden seguir ejecutandose localmente con `mvn spring-boot:run`

### HA readiness mode

- `docker-compose.yml` + `docker-compose.apps.yml` levantan infraestructura y los cinco servicios Spring Boot
- cada servicio usa hostnames internos Docker como `postgres`, `redis` y `rabbitmq`
- cada servicio tiene restart policy y healthcheck HTTP contra `GET /health`
- Prometheus scrapea `/actuator/prometheus` de los cinco servicios por nombre interno Docker

Tabla de componentes:

| Componente | Estado actual | Comentario |
| --- | --- | --- |
| `student-service` | Implementado | CRUD basico y modo contenedor disponible |
| `course-service` | Implementado | Catalogo academico, cache Redis y modo contenedor |
| `enrollment-service` | Implementado | Inscripciones, eventos RabbitMQ y modo contenedor |
| `billing-service` | Implementado | Cobros, eventos RabbitMQ y modo contenedor |
| `notification` | Implementado para evidencia | Consumidor RabbitMQ y modo contenedor |
| `gateway-service` | Preparado, no operativo | No participa en la demo actual |
| PostgreSQL | Implementado | Persistencia principal y healthcheck |
| Backup PostgreSQL | Implementado localmente | Scripts PowerShell y dumps en `infra/backups/output/` |
| Redis | Implementado | Cache de lectura y healthcheck |
| RabbitMQ | Implementado | Broker de eventos y healthcheck |
| Prometheus | Implementado con alcance local | Infra disponible, healthcheck activo y scrape real de los cinco microservicios en HA readiness mode |
| Grafana | Parcial | UI accesible; dashboards y alertas siguen pendientes |

[Insertar evidencia E02 - compose base e infraestructura]
[Insertar evidencia E03 - compose apps con servicios Spring Boot]

## 5. Infraestructura, endurecimiento Compose y observabilidad

Los cambios acumulados hasta S22 se enfocan en robustecer el entorno local sin romper el workflow actual:

- `restart: unless-stopped` para infraestructura y aplicaciones
- healthcheck de PostgreSQL con `pg_isready`
- healthcheck de Redis con `redis-cli ping`
- healthcheck de RabbitMQ con `rabbitmq-diagnostics -q ping`
- healthcheck de Prometheus con `http://localhost:9090/-/healthy`
- healthcheck de Grafana con `http://localhost:3000/api/health`
- healthchecks HTTP de servicios de aplicacion contra `GET /health`
- Actuator habilitado para `health`, `info` y `prometheus`
- Micrometer Prometheus registry agregado en los cinco microservicios
- scrape Prometheus apuntando a `student-service:8081`, `course-service:8082`, `enrollment-service:8083`, `billing-service:8084` y `notification:8085`
- scripts PowerShell para backup, restore y verificacion de PostgreSQL

Puertos operativos relevantes:

| Recurso | Puerto local | Nota |
| --- | --- | --- |
| PostgreSQL | `55432` | definido en `.env` actual |
| Redis | `6379` | por defecto |
| RabbitMQ AMQP | `5672` | por defecto |
| RabbitMQ UI | `15672` | `guest/guest` |
| Prometheus | `9090` | UI local y health endpoint |
| Grafana | `3000` | UI local y health endpoint |
| `student-service` | `8081` | `GET /health` |
| `course-service` | `8082` | `GET /health` |
| `enrollment-service` | `8083` | `GET /health` |
| `billing-service` | `8084` | `GET /health` |
| `notification` | `8085` | `GET /health` |

[Insertar evidencia E04 - healthchecks y restart policies]

## 6. Modo estandar

El modo estandar sigue siendo la base segura para desarrollo local y validacion gradual.

Comandos:

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

Uso esperado:

- levantar infraestructura compartida
- cargar la base de datos demo
- ejecutar microservicios con `mvn spring-boot:run` si el equipo prefiere el flujo local tradicional

## 7. Modo HA readiness

Este modo agrega los cinco microservicios como contenedores sin sustituir el modo estandar.

Comandos:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
```

Asuncion importante:

- si el volumen PostgreSQL es nuevo, primero debe ejecutarse la carga de `db/schema.sql` y `db/data.sql`

Este modo es suficiente para demostrar:

- empaquetado por servicio
- arranque coordinado
- healthchecks
- restart policies
- recuperacion manual de un servicio

No es suficiente para afirmar:

- replicas activas
- failover automatico
- balanceo de carga real
- orquestacion multinodo

[Insertar evidencia E05 - apps compose arriba]
[Insertar evidencia E06 - estado healthy en servicios]

## 8. Base de datos y carga determinista

La base de datos se prepara con:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Consulta minima de validacion:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_code, first_name, last_name, active FROM students ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, section_code, academic_period_id, course_id, capacity, active FROM sections ORDER BY id;"
```

El dataset demo deja una combinacion libre util para la validacion funcional:

- `studentId=1`
- `sectionId=2`

[Insertar evidencia E07 - carga de schema y seed]
[Insertar evidencia E08 - consulta de datos demo]

## 9. Backup, restore y recuperacion ante desastres S22

S22 agrega una capa local y defendible de recuperacion de datos sobre PostgreSQL. No cambia la logica de negocio ni el esquema. Agrega procedimientos simples de backup, restore y verificacion para el contenedor `campusenroll-postgres`.

Comandos:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
Get-ChildItem infra/backups/output
powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

Lo que protege:

- datos PostgreSQL de `students`, `courses`, `academic_periods`, `sections`, `schedule_blocks`, `enrollments` y `billings`

Lo que no protege:

- Redis
- RabbitMQ
- Prometheus/Grafana
- caches locales
- frontend, porque sigue fuera de alcance

Escenarios cubiertos por el runbook:

- perdida accidental de datos
- corrupcion del volumen PostgreSQL
- reconstruccion del entorno local

Mensaje honesto:

- esto fortalece la continuidad operativa local y la narrativa de HA readiness
- no equivale a backups programados, off-site, cifrados ni con retencion automatizada

[Insertar evidencia E24 - verificacion antes del backup]
[Insertar evidencia E25 - backup generado]
[Insertar evidencia E26 - restore con advertencia visible]
[Insertar evidencia E27 - verificacion despues del restore]
[Insertar evidencia E28 - runbook abierto]

## 10. Salud de servicios y healthchecks HTTP

Verificacion:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Cada servicio debe responder con un JSON que contenga su nombre y `status=UP`.

Interpretacion correcta:

- este healthcheck confirma disponibilidad basica del proceso
- no equivale por si solo a failover real entre replicas

[Insertar evidencia E09 - health checks HTTP]

## 11. Validacion funcional con Postman

Como no existe frontend, Postman sigue siendo el cliente operativo actual.

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

Flujo critico de negocio:

`Inscripcion de estudiante a una seccion y generacion de cobro asociado`

[Insertar evidencia E10 - Postman importado]
[Insertar evidencia E11 - flujo funcional principal]

## 12. Redis cache

`course-service` ya utiliza Redis para listas de catalogo.

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

[Insertar evidencia E12 - llaves Redis]

## 13. RabbitMQ y notification

RabbitMQ ya participa en el flujo de evidencia del repositorio.

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

[Insertar evidencia E13 - RabbitMQ bindings]
[Insertar evidencia E14 - logs de publicacion y consumo]

## 14. Validacion con k6

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

[Insertar evidencia E15 - smoke k6]
[Insertar evidencia E16 - 50,000 requests]
[Insertar evidencia E17 - concurrencia k6]

## 15. Prometheus y Grafana

Prometheus y Grafana forman parte del entorno local. En S21, Prometheus deja de ser solo infraestructura pasiva y pasa a scrapear metricas reales de los cinco microservicios cuando el modo HA readiness esta activo. Aun asi, esto no debe presentarse como una observabilidad completa de negocio o de produccion.

Prometheus:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml restart prometheus
curl.exe http://localhost:8081/actuator/prometheus
curl.exe http://localhost:8082/actuator/prometheus
curl.exe http://localhost:8083/actuator/prometheus
curl.exe http://localhost:8084/actuator/prometheus
curl.exe http://localhost:8085/actuator/prometheus
Start-Process 'http://localhost:9090/targets'
```

Grafana:

```powershell
Start-Process 'http://localhost:3000'
```

Estado honesto:

- Prometheus ya scrapea `prometheus` y las cinco aplicaciones en modo HA readiness
- los endpoints `GET /actuator/prometheus` tambien pueden verificarse desde host en `localhost:8081` a `localhost:8085`
- el workflow Maven local sigue intacto, pero los targets por nombre de servicio Docker solo apareceran `UP` cuando `docker-compose.apps.yml` este activo
- si el contenedor de Prometheus ya estaba corriendo desde antes, puede requerir un reinicio puntual para recargar la `prometheus.yml` montada
- Grafana sigue accesible, pero el repo todavia no entrega dashboards de negocio ni alertas listas

[Insertar evidencia E18 - Prometheus targets]
[Insertar evidencia E19 - endpoints actuator prometheus]
[Insertar evidencia E20 - Grafana accesible]

## 16. Recuperacion y observacion de fallas

### Recuperacion manual de un servicio en modo HA readiness

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml stop course-service
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
docker compose -f docker-compose.yml -f docker-compose.apps.yml start course-service
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
curl.exe http://localhost:8082/health
```

Lo que se demuestra:

- el servicio puede detenerse y levantarse otra vez dentro del stack Compose
- el healthcheck HTTP vuelve a marcar disponibilidad basica

### Falla controlada de infraestructura

```powershell
docker compose stop redis
curl.exe http://localhost:8082/api/courses
docker compose start redis
docker exec -i campusenroll-redis redis-cli ping
```

Lo que se demuestra:

- el catalogo puede seguir respondiendo aunque Redis este degradado
- Redis puede recuperarse y reinsertar llaves despues del siguiente acceso
- PostgreSQL ya tiene una ruta separada de recuperacion de datos mediante backup y restore local

[Insertar evidencia E21 - reinicio y recuperacion de course-service]
[Insertar evidencia E22 - fallback con Redis detenido]

## 17. Limites actuales y mejoras futuras

Los siguientes puntos deben quedar expresados como pendientes, no como trabajo ya completado:

- cluster multinodo real
- replicas activas por servicio
- balanceador de carga
- failover automatico
- replicacion de PostgreSQL
- backups programados
- almacenamiento off-site
- cifrado de backups
- politicas formales de retencion
- Redis cluster
- RabbitMQ cluster
- orquestacion con Kubernetes o Docker Swarm
- dashboards Grafana listos para plataforma y negocio
- alertas Prometheus/Grafana

## 18. Conclusiones

CampusEnroll HA ya puede presentarse como una solucion local `HA-ready` para la entrega: tiene infraestructura compartida endurecida, servicios de aplicacion contenedorizables, restart policies, healthchecks, cache Redis, eventos RabbitMQ, evidencia con Postman, validacion con k6, metricas Prometheus reales en los cinco microservicios y una capa local de backup/restore para PostgreSQL.

La conclusion correcta no es "ya existe alta disponibilidad real". La conclusion correcta es:

`la plataforma ya demuestra readiness local, recuperacion operativa basica, recuperacion manual de datos PostgreSQL y una postura de observabilidad mas fuerte para la entrega final, pero la alta disponibilidad productiva sigue siendo una mejora futura.`

[Insertar evidencia E23 - resumen final o cierre del PDF]
