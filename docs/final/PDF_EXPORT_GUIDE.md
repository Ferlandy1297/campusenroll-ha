# Guia de Exportacion a PDF

Este documento explica como convertir `docs/final/CHECKPOINT_1_PDF_READY.md` en el PDF final de entrega y como completar los insumos visuales que el Markdown no puede generar por si solo.

## 1. Archivo principal

El archivo base para el PDF final es:

- `docs/final/CHECKPOINT_1_PDF_READY.md`

Antes de exportar:

1. Completar placeholders de portada.
2. Renderizar diagramas.
3. Capturar e insertar screenshots reales.
4. Revisar ortografia, tablas y saltos de pagina.

## 2. Como renderizar los diagramas

### 2.1 Diagramas PlantUML desde `docs/diagrams/`

Archivos:

- `docs/diagrams/architecture.puml`
- `docs/diagrams/use-cases.puml`
- `docs/diagrams/critical-sequence.puml`
- `docs/diagrams/components.puml`
- `docs/diagrams/event-flow.puml`

Opcion con Docker:

```powershell
docker run --rm -v "${PWD}:/work" -w /work plantuml/plantuml -tpng `
  docs/diagrams/architecture.puml `
  docs/diagrams/use-cases.puml `
  docs/diagrams/critical-sequence.puml `
  docs/diagrams/components.puml `
  docs/diagrams/event-flow.puml
```

Opcion con PlantUML local:

```powershell
plantuml -tpng docs/diagrams/architecture.puml
plantuml -tpng docs/diagrams/use-cases.puml
plantuml -tpng docs/diagrams/critical-sequence.puml
plantuml -tpng docs/diagrams/components.puml
plantuml -tpng docs/diagrams/event-flow.puml
```

Recomendacion:

- Exportar en PNG o SVG.
- Insertar cada diagrama en su seccion correspondiente del documento final.
- No modificar los `.puml` en este segmento; solo renderizarlos.

### 2.2 Modelo ER desde DBML

Archivo:

- `docs/diagrams/er-model.dbml`

Opcion recomendada:

1. Abrir https://dbdiagram.io
2. Crear un diagrama nuevo.
3. Importar o pegar el contenido de `docs/diagrams/er-model.dbml`.
4. Ajustar zoom y legibilidad.
5. Exportar imagen o capturar pantalla con calidad suficiente.

## 3. Donde insertar screenshots

El documento `CHECKPOINT_1_PDF_READY.md` ya incluye placeholders del tipo:

- `[Insertar evidencia E01 - ...]`
- `[Insertar diagrama D01 - ...]`

Usar `docs/final/EVIDENCE_PLACEHOLDERS.md` para mapear:

- nombre sugerido del archivo de captura
- que debe mostrar la captura
- en que seccion del documento debe insertarse

Regla practica:

- evidencias de repositorio y Postman: cerca de la seccion 3
- reglas y conflictos: cerca de las secciones 4, 7 y 15
- diagramas renderizados: secciones 5 a 10
- observabilidad e infraestructura: secciones 12 a 15

## 4. Opcion A: Word o Google Docs

Esta es la opcion mas segura si se desea ajustar formato academico manualmente.

Pasos:

1. Abrir `docs/final/CHECKPOINT_1_PDF_READY.md`.
2. Copiar todo el contenido.
3. Pegar en Word o Google Docs.
4. Aplicar estilo de portada, encabezados, tablas, numeracion y saltos de pagina.
5. Insertar diagramas renderizados e imagenes reales.
6. Exportar como PDF.

Ventajas:

- Control fino del formato final.
- Facil insercion manual de capturas y portada institucional.
- Mejor manejo de saltos de pagina, margenes y encabezados.

## 5. Opcion B: VS Code con extension Markdown PDF

Si se usa Visual Studio Code:

1. Instalar la extension `Markdown PDF`.
2. Abrir `docs/final/CHECKPOINT_1_PDF_READY.md`.
3. Verificar que las imagenes insertadas apunten a rutas validas si ya se agregaron referencias locales.
4. Ejecutar `Markdown PDF: Export (pdf)` desde la paleta de comandos.

Notas:

- Esta opcion es util si el documento ya tiene imagenes vinculadas por ruta.
- Si las capturas solo existen fuera del Markdown, Word o Google Docs suele ser mas comodo.

## 6. Opcion C: Pandoc

Si Pandoc ya esta instalado, puede generar un PDF o un DOCX intermedio.

Ejemplo a DOCX:

```powershell
pandoc docs/final/CHECKPOINT_1_PDF_READY.md -o docs/final/CHECKPOINT_1_PDF_READY.docx
```

Ejemplo a PDF:

```powershell
pandoc docs/final/CHECKPOINT_1_PDF_READY.md -o docs/final/CHECKPOINT_1_PDF_READY.pdf
```

Notas:

- La calidad del PDF depende del motor de render disponible en la maquina.
- Si el resultado visual no queda suficientemente academico, exportar primero a DOCX y ajustar en Word.

## 7. Orden recomendado del PDF final

Orden sugerido para la version final:

1. Portada
2. Dominio asignado y contexto del problema
3. Estado actual del proyecto
4. Reglas criticas de negocio
5. Diagrama de arquitectura
6. Diagrama de casos de uso
7. Diagrama de secuencia del caso critico
8. Diagrama de componentes
9. Diagrama de flujo de eventos
10. Modelo entidad-relacion
11. Estrategia de consistencia
12. Estrategia de alta disponibilidad
13. Estrategia de cache
14. Plan de observabilidad
15. Plan de pruebas finales
16. Evidencias visuales sugeridas
17. Conclusiones y plan de cierre

## 8. Checklist final antes de entregar

- Verificar que el segmento diga `S16`.
- Completar curso, seccion, docente, integrantes y fecha.
- Confirmar que la URL del repositorio GitHub este correcta.
- Confirmar que el flujo critico aparezca exactamente como:
  - `Inscripcion de estudiante a una seccion y generacion de cobro asociado`
- Confirmar que no se afirma lo siguiente como implementado:
  - cache Redis dentro de la logica de negocio
  - eventos RabbitMQ operativos
  - dashboards Prometheus/Grafana completos
  - API Gateway como entrypoint activo del flujo actual
- Insertar capturas reales y legibles.
- Verificar que los diagramas renderizados correspondan a las fuentes actuales.
- Revisar numeracion de secciones, encabezados y tablas.
- Exportar el PDF y revisar una ultima vez el archivo final antes de subirlo.
