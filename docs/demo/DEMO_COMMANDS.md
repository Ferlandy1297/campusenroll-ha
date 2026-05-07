# Demo Commands - PowerShell

## Uso

Estos comandos son de apoyo para la demostracion. Son seguros y no modifican codigo del proyecto. Ejecutarlos desde la raiz del repositorio `campusenroll-ha`, salvo que se indique lo contrario.

## 1. Estado de Git

```powershell
git status
```

```powershell
git log --oneline --graph --decorate --all -n 20
```

## 2. Infraestructura con Docker Compose

Levantar infraestructura local:

```powershell
docker compose up -d
```

Ver estado de contenedores:

```powershell
docker compose ps
```

Ver logs generales:

```powershell
docker compose logs --tail 50
```

Ver logs por servicio:

```powershell
docker compose logs --tail 50 postgres
docker compose logs --tail 50 redis
docker compose logs --tail 50 rabbitmq
docker compose logs --tail 50 prometheus
docker compose logs --tail 50 grafana
```

## 3. Inspeccion rapida de archivos de base de datos

Ver README de base de datos:

```powershell
Get-Content .\db\README.md
```

Ver inicio de `schema.sql`:

```powershell
Get-Content .\db\schema.sql -TotalCount 120
```

Ver inicio de `data.sql`:

```powershell
Get-Content .\db\data.sql -TotalCount 120
```

Buscar tablas clave dentro del esquema:

```powershell
Select-String -Path .\db\schema.sql -Pattern "CREATE TABLE students|CREATE TABLE sections|CREATE TABLE enrollments|CREATE TABLE billings"
```

## 4. Verificacion rapida de PostgreSQL en contenedor

Listar tablas:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\dt"
```

Consultar estudiantes:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_code, first_name, last_name, active FROM students ORDER BY id;"
```

Consultar secciones:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, section_code, academic_period_id, course_id, capacity, active FROM sections ORDER BY id;"
```

Consultar inscripciones:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status, enrolled_at FROM enrollments ORDER BY id;"
```

Consultar cobros:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, amount, currency, status, created_at FROM billings ORDER BY id;"
```

## 5. Health checks con curl

Nota: en PowerShell se recomienda `curl.exe` para evitar el alias nativo.

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

## 6. Consultas GET utiles durante la demo

```powershell
curl.exe http://localhost:8081/api/students
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/periods
curl.exe http://localhost:8082/api/sections
curl.exe http://localhost:8083/api/enrollments
curl.exe http://localhost:8084/api/billings
```

## 7. Referencias utiles de documentacion durante la demo

```powershell
Get-Content .\postman\README.md
Get-Content .\infra\k6\README.md
Get-Content .\docs\checkpoint\CHECKPOINT_1_REVISION_TECNICA.md
Get-Content .\docs\diagrams\README.md
```

## 8. k6 con Docker - opcional

Smoke test:

```powershell
docker run --rm -i `
  --network host `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

Escenario de 50,000 requests:

```powershell
docker run --rm -i `
  --network host `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/load-50000-requests.js
```

Escenario concurrente de inscripcion:

```powershell
docker run --rm -i `
  --network host `
  -e ENROLLMENT_SERVICE_URL=http://localhost:8083 `
  -e TEST_STUDENT_ID=1 `
  -e TEST_SECTION_ID=1 `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/concurrent-enrollment-test.js
```

## 9. Nota de seguridad

Este archivo evita comandos destructivos a proposito. Si se requiere limpieza de contenedores o eliminacion de volumenes, hacerlo fuera de la demo y con validacion previa, porque esos pasos no son necesarios para la exposicion del checkpoint.
