# Application Failover / Switchover Demo - S25

## Objetivo

S25 agrega una capa segura y demostrable de failover y switchover a nivel de aplicacion para `course-service` sin cambiar la regla academica de PostgreSQL centralizado y sin modificar la logica de negocio Java.

## Que agrega el repositorio ahora

- `docker-compose.ha-demo.yml`
  - agrega `course-service-replica` y el servicio `haproxy`
- `infra/load-balancer/haproxy.cfg`
  - publica un gateway HTTP para el catalogo academico
- `infra/ha/failover-demo.ps1`
  - automatiza la caida controlada del primario y la verificacion del gateway
- `infra/ha/switchover-demo.ps1`
  - automatiza el switchover planeado por mantenimiento

## Diferencia correcta entre failover y switchover

- `Failover`
  - cambio de trafico cuando el servicio primario deja de estar disponible
- `Switchover`
  - cambio de trafico intencional y controlado por mantenimiento o por una prueba planeada

En este repositorio ambos conceptos se demuestran en la capa de aplicacion, no en la capa de base de datos.

## Diferencia con failover de base de datos

Lo implementado en S25:

- HAProxy recibe las solicitudes del catalogo
- HAProxy vigila `course-service` y `course-service-replica` con `GET /health`
- si el primario falla, HAProxy envia el trafico a la replica

Lo que no existe en S25:

- replicacion PostgreSQL
- failover automatico de PostgreSQL
- Patroni
- repmgr
- pg_auto_failover
- base de datos administrada con conmutacion automatica

Mensaje exacto para defensa:

`CampusEnroll HA ahora demuestra failover y switchover a nivel de aplicacion para course-service usando HAProxy y una replica. PostgreSQL failover no esta implementado; la recuperacion de base de datos sigue siendo backup y restore.`

## Topologia del demo

Capas Compose:

- `docker-compose.yml`
  - PostgreSQL, Redis, RabbitMQ, Prometheus y Grafana
- `docker-compose.apps.yml`
  - los cinco servicios Spring Boot originales
- `docker-compose.ha-demo.yml`
  - `course-service-replica`
  - `haproxy`

Puertos relevantes:

- `http://localhost:8080`
  - gateway HAProxy para `course-service`
- `http://localhost:8404/stats`
  - stats locales de HAProxy
- `http://localhost:8082`
  - acceso host directo al `course-service` primario

Rutas expuestas por HAProxy en esta demo:

- `GET /api/courses`
- `GET /api/periods`
- `GET /api/sections`
- `GET /health/course`

## Comando de arranque

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

## Verificacion minima

```powershell
curl.exe -i http://localhost:8080/api/courses
curl.exe -i http://localhost:8080/health/course
Start-Process "http://localhost:8404/stats"
```

Interpretacion:

- si el gateway responde `HTTP 200`, HAProxy esta alcanzando el backend activo
- si `/health/course` responde `HTTP 200`, al menos una instancia de `course-service` esta sana

## Demo de failover

Pasos manuales:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml stop course-service
curl.exe -i http://localhost:8080/api/courses
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml start course-service
curl.exe -i http://localhost:8080/api/courses
```

Resultado esperado:

- el primario queda detenido
- HAProxy deja de usar el primario al fallar su `GET /health`
- `http://localhost:8080/api/courses` sigue devolviendo `HTTP 200`
- la continuidad la sostiene `course-service-replica`

Atajo con script:

```powershell
powershell -ExecutionPolicy Bypass -File infra/ha/failover-demo.ps1 -OpenStats
```

## Demo de switchover planeado

Narrativa correcta:

- no se espera una caida accidental
- el equipo saca el primario de rotacion por mantenimiento
- la replica mantiene el servicio disponible
- luego el primario vuelve al pool

Pasos manuales:

```powershell
curl.exe -i http://localhost:8080/api/courses
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml stop course-service
curl.exe -i http://localhost:8080/api/courses
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml start course-service
curl.exe -i http://localhost:8080/api/courses
```

Atajo con script:

```powershell
powershell -ExecutionPolicy Bypass -File infra/ha/switchover-demo.ps1 -OpenStats
```

## Como detecta HAProxy una falla

- ejecuta `GET /health` contra `course-service:8082`
- ejecuta `GET /health` contra `course-service-replica:8082`
- si el primario no responde `200 OK`, HAProxy lo marca como no disponible
- la replica queda lista para responder el trafico del catalogo

## Lo implementado actualmente

- replica local de `course-service`
- gateway HAProxy local
- health-check HTTP para ambos backends
- continuidad para el catalogo academico si cae el proceso del `course-service` primario
- demo de failover y switchover a nivel de aplicacion

## Lo que sigue siendo futuro

- replica equivalente para `student-service`, `enrollment-service`, `billing-service` y `notification`
- entrypoint unificado para todos los microservicios
- failover de PostgreSQL
- replicacion de PostgreSQL
- HA de Redis
- HA de RabbitMQ
- orquestacion con Kubernetes o Docker Swarm

## Respuesta corta si preguntan por PostgreSQL failover

`No. Este segmento implementa failover y switchover de la aplicacion course-service a traves de HAProxy. PostgreSQL sigue siendo una base centralizada y la recuperacion actual se hace con backup y restore. Para HA real de base de datos harian falta replicacion y una herramienta como Patroni, repmgr, pg_auto_failover o un servicio administrado equivalente.`
