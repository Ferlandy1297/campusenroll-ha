# Demo Folder - CampusEnroll HA

## Proposito

Esta carpeta agrupa el material de apoyo para presentar y sustentar la `Revision Tecnica Avanzada del Proyecto Final` de CampusEnroll HA.

Su objetivo es ayudar a:

- conducir una demo corta y consistente
- preparar capturas y evidencia antes del envio
- tener a mano comandos seguros de apoyo durante la exposicion

Este material no reemplaza el PDF final del checkpoint. Lo complementa y facilita su preparacion.

## Archivos

- `docs/demo/DEMO_SCRIPT.md`
  - guion sugerido de 5 a 8 minutos en espanol
- `docs/demo/EVIDENCE_CHECKLIST.md`
  - checklist de capturas y evidencia requerida antes de enviar
- `docs/demo/DEMO_COMMANDS.md`
  - comandos PowerShell utiles y seguros para la demo
- `docs/demo/README.md`
  - descripcion y orden recomendado de uso

## Orden recomendado de uso

1. Leer `DEMO_SCRIPT.md` para preparar la narrativa.
2. Revisar `DEMO_COMMANDS.md` para tener terminal y verificaciones listas.
3. Ejecutar o ensayar la demo con Postman y los documentos tecnicos abiertos.
4. Completar `EVIDENCE_CHECKLIST.md` mientras se capturan las pruebas visuales.
5. Consolidar las capturas en el PDF final del checkpoint.

## Alcance y limites

- Las capturas reales deben tomarse manualmente.
- Este folder documenta el estado actual del proyecto y no debe usarse para afirmar funcionalidades no implementadas.
- Postman es el cliente actual del sistema porque todavia no existe frontend.
- Redis, RabbitMQ, Prometheus y Grafana deben presentarse como infraestructura disponible o preparada, no como integracion completa de negocio si esa evidencia no existe.

## Recomendacion practica

Para una demo corta y ordenada, tener abiertos al mismo tiempo:

- el repositorio
- Postman
- `docs/checkpoint/CHECKPOINT_1_REVISION_TECNICA.md`
- `docs/demo/DEMO_SCRIPT.md`
- una terminal PowerShell en la raiz del proyecto
