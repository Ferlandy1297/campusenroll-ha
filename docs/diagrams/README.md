# Diagramas de CampusEnroll HA

Esta carpeta contiene las fuentes del checkpoint tecnico:

- `architecture.puml`
- `use-cases.puml`
- `critical-sequence.puml`
- `components.puml`
- `event-flow.puml`
- `er-model.dbml`

## Como renderizar los diagramas PlantUML

Opcion con Docker:

```powershell
docker run --rm -v "${PWD}:/work" -w /work plantuml/plantuml -tsvg `
  docs/diagrams/architecture.puml `
  docs/diagrams/use-cases.puml `
  docs/diagrams/critical-sequence.puml `
  docs/diagrams/components.puml `
  docs/diagrams/event-flow.puml
```

Opcion con PlantUML local:

```powershell
plantuml -tsvg docs/diagrams/architecture.puml
plantuml -tsvg docs/diagrams/use-cases.puml
plantuml -tsvg docs/diagrams/critical-sequence.puml
plantuml -tsvg docs/diagrams/components.puml
plantuml -tsvg docs/diagrams/event-flow.puml
```

Notas:

- Los `.puml` distinguen visualmente lo implementado, lo preparado y lo pendiente.
- No es necesario modificar codigo backend para regenerar estos diagramas.

## Como usar el archivo DBML

Archivo:

- `docs/diagrams/er-model.dbml`

Uso recomendado en dbdiagram.io:

1. Abrir https://dbdiagram.io
2. Crear un diagrama nuevo.
3. Importar o pegar el contenido de `er-model.dbml`.
4. Revisar relaciones, claves y notas del modelo.

Notas:

- El DBML esta alineado con `db/schema.sql`.
- Las restricciones parciales especificas de PostgreSQL se documentan como notas cuando DBML no las expresa de forma nativa.
