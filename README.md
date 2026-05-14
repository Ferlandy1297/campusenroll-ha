# CampusEnroll HA

## Estado actual

CampusEnroll HA ya no es un esqueleto S00. El repositorio contiene implementacion funcional para:

- `student-service` con endpoints de estudiantes
- `course-service` con endpoints de catalogo y cache Redis para listas de cursos, periodos y secciones
- `enrollment-service` con endpoints de inscripcion y publicacion de `EnrollmentCreatedEvent` en RabbitMQ
- `billing-service` con endpoints de cobro y publicacion de `BillingStatusChangedEvent` en RabbitMQ
- `notification` como consumidor de eventos RabbitMQ para evidencia y logs
- `db/schema.sql` y `db/data.sql` como carga determinista de PostgreSQL
- `postman/` como cliente operativo actual
- `infra/k6/` como paquete de smoke, 50,000 requests, concurrencia y observacion de falla

Estado honesto del proyecto:

- Docker Compose levanta solo infraestructura compartida: PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana.
- Los servicios Spring Boot se ejecutan localmente con `mvn spring-boot:run`.
- No existe frontend.
- `gateway-service` sigue siendo una referencia arquitectonica, no el entrypoint operativo del flujo actual.
- Prometheus y Grafana estan disponibles como infraestructura, pero el repositorio todavia no entrega metricas de aplicacion scrapeadas ni dashboards provisionados listos.

## Estructura

- `backend/`
  - servicios Spring Boot por dominio
- `db/`
  - esquema PostgreSQL y datos demo
- `docs/checkpoint/`
  - resumen tecnico del estado actual
- `docs/demo/`
  - guia de demo, comandos PowerShell y checklist de evidencia
- `docs/final/`
  - documento PDF-ready, placeholders y guia de exportacion
- `infra/k6/`
  - scripts y runbook de pruebas finales
- `postman/`
  - coleccion y environment local

## Flujo de validacion local

La validacion final del repositorio debe seguir este orden:

1. Ajustar `.env` si hay conflicto con PostgreSQL.
   - El `.env` actual del repo usa `POSTGRES_PORT=55432`.
   - Si se mantiene `5432`, los `jdbc:postgresql://localhost:PUERTO/campusenroll` de los servicios deben coincidir con ese puerto.
2. Levantar infraestructura:

```powershell
docker compose up -d postgres redis rabbitmq prometheus grafana
docker compose ps
```

3. Cargar base de datos determinista:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

4. Ejecutar los servicios en terminales separadas con `mvn spring-boot:run`.
   - Ver la secuencia exacta en `docs/demo/DEMO_COMMANDS.md`.
5. Verificar salud:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

6. Ejecutar el flujo funcional y las pruebas finales.
   - Postman: `postman/README.md`
   - Demo y evidencia: `docs/demo/`
   - Documento final: `docs/final/`
   - k6 y falla controlada: `infra/k6/README.md`

## URLs utiles

- RabbitMQ Management UI: `http://localhost:15672` con `guest/guest`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` con `admin/admin` salvo cambio local

## Documentacion recomendada

- `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md`
- `docs/demo/DEMO_SCRIPT.md`
- `docs/demo/DEMO_COMMANDS.md`
- `docs/demo/EVIDENCE_CHECKLIST.md`
- `docs/final/CHECKPOINT_1_PDF_READY.md`
- `docs/final/EVIDENCE_PLACEHOLDERS.md`
- `docs/final/PDF_EXPORT_GUIDE.md`
