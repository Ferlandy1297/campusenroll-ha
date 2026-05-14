# Demo Folder - CampusEnroll HA

## Proposito

`docs/demo/` concentra la guia practica para la entrega final de S19. El objetivo ya no es describir un checkpoint hipotetico, sino ejecutar una validacion corta, repetible y honesta del estado actual del repositorio.

## Que cubre

- `DEMO_SCRIPT.md`
  - narrativa de 5 a 8 minutos para la exposicion
- `DEMO_COMMANDS.md`
  - comandos PowerShell exactos para infraestructura, base de datos, servicios, Postman, Redis, RabbitMQ, k6 y observabilidad
- `EVIDENCE_CHECKLIST.md`
  - lista de capturas y salidas que deben quedar guardadas para la entrega final

## Orden recomendado

1. Leer `DEMO_SCRIPT.md`.
2. Abrir una terminal PowerShell en la raiz del repo.
3. Ejecutar `DEMO_COMMANDS.md`.
4. Importar Postman y validar el flujo funcional.
5. Completar `EVIDENCE_CHECKLIST.md`.

## Mensajes que no deben perderse en la demo

- Postman es el cliente actual porque no existe frontend.
- Docker Compose levanta infraestructura, no los microservicios Spring Boot.
- Redis si participa hoy en `course-service`.
- RabbitMQ si participa hoy para publicacion y consumo de eventos de evidencia.
- Prometheus y Grafana existen como infraestructura disponible, pero la observabilidad de aplicacion sigue parcial.
