# Documentacion Final

## Proposito

`docs/final/` agrupa la fuente documental para el paquete final de entrega de CampusEnroll HA despues de S19.

## Archivos

- `CHECKPOINT_1_PDF_READY.md`
  - documento principal listo para copiar a Word, Google Docs o exportar con una herramienta Markdown
- `EVIDENCE_PLACEHOLDERS.md`
  - mapa de capturas y salidas sugeridas
- `PDF_EXPORT_GUIDE.md`
  - pasos de exportacion y checklist final

## Enfoque de esta carpeta

El contenido ya esta alineado con el estado real del repo:

- Redis si esta implementado en `course-service`
- RabbitMQ si participa en publicacion y consumo de eventos de evidencia
- Postman sigue siendo el cliente actual
- Docker Compose sigue levantando solo infraestructura
- Prometheus y Grafana siguen siendo infraestructura disponible con integracion parcial

## Relacion con otras carpetas

- `docs/checkpoint/`
  - resumen tecnico corto del estado actual
- `docs/demo/`
  - runbook de demo y evidencia en PowerShell
- `postman/`
  - coleccion y environment local
- `infra/k6/`
  - scripts y guia de pruebas finales
