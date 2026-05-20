# Demo Folder - CampusEnroll HA

## Proposito

`docs/demo/` concentra la guia practica para la entrega final de S20. El objetivo es ejecutar una validacion corta, repetible y honesta del estado actual del repositorio, incluyendo el nuevo modo de HA readiness con Docker Compose.

## Que cubre

- `DEMO_SCRIPT.md`
  - narrativa de 5 a 8 minutos para la exposicion
- `DEMO_COMMANDS.md`
  - comandos PowerShell exactos para standard mode, HA readiness mode, base de datos, Postman, Redis, RabbitMQ, k6 y observabilidad
- `EVIDENCE_CHECKLIST.md`
  - lista de capturas y salidas para la entrega final

## Orden recomendado

1. Leer `DEMO_SCRIPT.md`.
2. Abrir una terminal PowerShell en la raiz del repo.
3. Ejecutar `DEMO_COMMANDS.md`.
4. Importar Postman y validar el flujo funcional.
5. Completar `EVIDENCE_CHECKLIST.md`.

## Mensajes que no deben perderse en la demo

- `docker-compose.yml` es standard mode para infraestructura compartida.
- `docker-compose.apps.yml` agrega un modo demostrable de HA readiness para los cinco servicios Spring Boot.
- Postman sigue siendo el cliente actual porque no existe frontend.
- Redis si participa hoy en `course-service`.
- RabbitMQ si participa hoy para publicacion y consumo de eventos de evidencia.
- Prometheus y Grafana existen como infraestructura disponible.
- El estado actual es readiness local reforzado, no alta disponibilidad productiva.
