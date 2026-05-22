# Presentation Q&A - S25

## Que tipo de alta disponibilidad si esta implementada

`Alta disponibilidad a nivel de aplicacion para course-service.`  
Se logra con HAProxy delante de `course-service` y `course-service-replica`.

## Que tipo de alta disponibilidad no esta implementada

`No existe failover de PostgreSQL.`  
La base sigue siendo centralizada por requisito academico y su recuperacion actual es manual mediante backup y restore.

## Que detecta HAProxy

HAProxy ejecuta `GET /health` sobre las dos instancias de `course-service`. Si el primario deja de responder `200`, lo saca de rotacion.

## Que prueba concreta hace la demo

- el gateway responde en `http://localhost:8080`
- el primario `course-service` se detiene
- `http://localhost:8080/api/courses` sigue devolviendo `HTTP 200`
- el primario se vuelve a iniciar

## Esto es Kubernetes

No. El demo usa Docker Compose mas HAProxy.

## Esto es replicacion de base de datos

No. Ambos `course-service` comparten la misma base PostgreSQL centralizada.

## Esto reemplaza backup y restore

No. Backup y restore siguen siendo la estrategia actual de recuperacion de datos PostgreSQL.

## Esto cubre todos los microservicios

No. S25 cubre solo `course-service` y las rutas del catalogo academico.

## Como responder si preguntan por Patroni o repmgr

`No estan implementados en este repo.`  
Se mencionan solo como ejemplos de futuro trabajo para failover real de PostgreSQL.
