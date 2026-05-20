# CampusEnroll HA

## Estado actual

CampusEnroll HA ya tiene una base funcional para:

- `student-service` con endpoints de estudiantes
- `course-service` con catalogo academico y cache Redis
- `enrollment-service` con inscripciones y publicacion de `EnrollmentCreatedEvent`
- `billing-service` con cobros y publicacion de `BillingStatusChangedEvent`
- `notification` como consumidor RabbitMQ para evidencia y logs
- `db/schema.sql` y `db/data.sql` para carga determinista de PostgreSQL
- `postman/` como cliente operativo actual
- `infra/k6/` como paquete de validacion final

La entrega actual ya no depende solo del flujo local con Maven. El repo ahora soporta dos modos de ejecucion sin reemplazar el workflow existente.

## Modos de ejecucion

### 1. Standard mode

`docker-compose.yml` sigue levantando solo infraestructura compartida:

- PostgreSQL
- Redis
- RabbitMQ
- Prometheus
- Grafana

Comando base:

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

Este modo mantiene intacto el flujo local actual con `mvn spring-boot:run`.

### 2. HA readiness mode

`docker-compose.apps.yml` agrega los cinco servicios Spring Boot como contenedores:

- `student-service` en `8081`
- `course-service` en `8082`
- `enrollment-service` en `8083`
- `billing-service` en `8084`
- `notification` en `8085`

Comando de arranque:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps
```

Importante:

- este modo es `HA-ready` y demostrable para la entrega, no alta disponibilidad productiva
- la primera ejecucion sobre un volumen PostgreSQL nuevo todavia requiere cargar `db/schema.sql` y `db/data.sql`
- el workflow con Maven local sigue siendo valido y no fue removido

## Flujo local recomendado

1. Confirmar `.env`.
2. Levantar infraestructura compartida.
3. Cargar base de datos demo si el volumen es nuevo.
4. Elegir uno de estos caminos:
   - ejecutar servicios localmente con `mvn spring-boot:run`
   - ejecutar servicios con `docker-compose.apps.yml`
5. Verificar salud con `GET /health`.
6. Ejecutar Postman, Redis, RabbitMQ, k6 y la evidencia final.

Carga de base de datos:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Health checks:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

## Que esta implementado

- `restart: unless-stopped` en infraestructura y servicios de aplicacion en modo Compose
- healthchecks para PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana y los cinco servicios Spring Boot
- contenedorizacion de los cinco servicios de negocio mediante `docker-compose.apps.yml`
- Redis real en `course-service`
- RabbitMQ real para publicacion y consumo de eventos de evidencia
- k6 como paquete de validacion final
- Prometheus y Grafana como infraestructura disponible

## Que sigue siendo mejora futura

- cluster multinodo real
- replicacion o failover de PostgreSQL
- Redis cluster
- RabbitMQ cluster
- balanceador real con replicas multiples
- Kubernetes o Docker Swarm
- scrapeo Prometheus de microservicios y dashboards Grafana listos
- gateway operativo como entrypoint real

## URLs utiles

- RabbitMQ Management UI: `http://localhost:15672`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## Documentacion recomendada

- `docs/demo/DEMO_COMMANDS.md`
- `docs/demo/EVIDENCE_CHECKLIST.md`
- `docs/final/CHECKPOINT_1_PDF_READY.md`
- `docs/final/EVIDENCE_PLACEHOLDERS.md`
- `infra/k6/README.md`
