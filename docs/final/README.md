# Documentacion Final

## Proposito

`docs/final/` agrupa la fuente documental para el paquete final de entrega de CampusEnroll HA despues de S20.

## Archivos

- `CHECKPOINT_1_PDF_READY.md`
  - documento principal listo para copiar a Word, Google Docs o exportar con una herramienta Markdown
- `EVIDENCE_PLACEHOLDERS.md`
  - mapa de capturas y salidas sugeridas
- `PDF_EXPORT_GUIDE.md`
  - pasos de exportacion y checklist final

## Enfoque de esta carpeta

El contenido esta alineado con el estado real del repo:

- `docker-compose.yml` sigue siendo standard mode para infraestructura
- `docker-compose.apps.yml` agrega el modo HA readiness para aplicaciones
- Redis si esta implementado en `course-service`
- RabbitMQ si participa en publicacion y consumo de eventos de evidencia
- Postman sigue siendo el cliente actual
- Prometheus y Grafana siguen siendo infraestructura disponible
- la plataforma es demostrable como `HA-ready`, no como HA productiva

## Relacion con otras carpetas

- `docs/demo/`
  - runbook de demo y evidencia en PowerShell
- `postman/`
  - coleccion y environment local
- `infra/k6/`
  - scripts y guia de pruebas finales
