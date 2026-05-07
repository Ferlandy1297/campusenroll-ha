# Documentacion Final

## Proposito

`docs/final/` centraliza la fuente documental para armar el PDF final de la `Revision Tecnica Avanzada del Proyecto Final` de CampusEnroll HA.

Esta carpeta no reemplaza la evidencia manual ni los artefactos tecnicos existentes del repositorio. Su funcion es consolidar:

- el documento principal listo para exportar
- la guia de exportacion a PDF
- el mapa de placeholders para capturas

## Archivo principal

El archivo principal es:

- `docs/final/CHECKPOINT_1_PDF_READY.md`

Ese archivo debe usarse como base para:

- copiar a Word o Google Docs
- exportar con una extension de Markdown
- exportar con Pandoc si ya esta instalado

## Relacion con otras carpetas de documentacion

- `docs/checkpoint/`
  - contiene la revision tecnica base ya redactada para el checkpoint previo
  - sirve como antecedente tecnico del contenido final
- `docs/diagrams/`
  - contiene las fuentes `.puml` y `.dbml`
  - se usa para renderizar los diagramas que luego se insertan en el PDF
- `docs/demo/`
  - contiene guion, comandos y checklist de evidencia para preparar la exposicion y las capturas
  - complementa el PDF, pero no lo sustituye
- `docs/final/`
  - consolida el documento final PDF-ready y la guia operativa para cerrarlo

## Archivos de esta carpeta

- `docs/final/CHECKPOINT_1_PDF_READY.md`
  - fuente principal del PDF final
- `docs/final/PDF_EXPORT_GUIDE.md`
  - instrucciones para renderizar diagramas, insertar capturas y exportar a PDF
- `docs/final/EVIDENCE_PLACEHOLDERS.md`
  - listado de capturas sugeridas y donde insertarlas
- `docs/final/README.md`
  - descripcion de esta carpeta

## Nota importante sobre screenshots

Las capturas reales deben obtenerse manualmente. Este segmento no genera screenshots por si solo y no debe inventar evidencia visual.

Antes de exportar el PDF final:

1. capturar las evidencias reales
2. renderizar los diagramas
3. insertar imagenes donde corresponda
4. revisar que el documento no afirme funcionalidades no implementadas
