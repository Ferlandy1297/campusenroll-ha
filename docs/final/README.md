# Documentacion Final

## Proposito

`docs/final/` agrupa la fuente documental para el paquete final de entrega de CampusEnroll HA despues de S33.

## Archivos

- `CHECKPOINT_1_PDF_READY.md`
  - documento principal listo para copiar a Word, Google Docs o exportar con una herramienta Markdown
- `FINAL_REGRESSION_CHECKLIST.md`
  - checklist operativo unico para validar el estado final antes de la presentacion
- `FINAL_EVIDENCE_PACKAGE.md`
  - lista de capturas, salidas y pruebas que deben reunirse como evidencia final
- `FINAL_PRESENTATION_READINESS.md`
  - inventario final, lenguaje honesto y defensa corta de preguntas frecuentes
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
- reglas activas de Prometheus ya existen, pero Alertmanager y dashboards productivos siguen pendientes
- S22 agrega backup, restore y runbook de recuperacion para PostgreSQL
- S32 agrega un demo aislado de replicacion streaming de PostgreSQL con primary, replica y promocion manual
- S33 agrega el paquete final de regresion, evidencia y readiness
- la plataforma es demostrable como `HA-ready`, no como HA productiva

## Relacion con otras carpetas

- `docs/demo/`
  - runbook de demo y evidencia en PowerShell
- `postman/`
  - coleccion y environment local
- `infra/k6/`
  - scripts y guia de pruebas finales
- `infra/backups/`
  - scripts y runbook de backup y recuperacion de base de datos
- `infra/postgres-ha/`
  - assets y runbook del demo aislado de replicacion PostgreSQL S32
