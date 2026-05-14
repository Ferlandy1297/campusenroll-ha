# Guia de Exportacion a PDF

## 1. Archivo fuente principal

Usar:

- `docs/final/CHECKPOINT_1_PDF_READY.md`

Antes de exportar:

1. completar placeholders de portada
2. insertar capturas reales
3. revisar tablas y saltos de pagina
4. verificar que el texto no afirme capacidades no implementadas

## 2. Evidencia obligatoria

La guia practica de capturas ya esta en:

- `docs/final/EVIDENCE_PLACEHOLDERS.md`
- `docs/demo/EVIDENCE_CHECKLIST.md`

## 3. Opcion A - Word o Google Docs

Ruta recomendada si el equipo quiere control manual del formato:

1. abrir `CHECKPOINT_1_PDF_READY.md`
2. copiar el contenido
3. pegar en Word o Google Docs
4. insertar capturas reales
5. ajustar portada, encabezados, tablas y saltos de pagina
6. exportar a PDF

## 4. Opcion B - VS Code con Markdown PDF

1. instalar una extension tipo `Markdown PDF`
2. abrir `docs/final/CHECKPOINT_1_PDF_READY.md`
3. verificar que las imagenes insertadas tengan rutas validas
4. ejecutar la exportacion a PDF

## 5. Opcion C - Pandoc

Si Pandoc esta disponible:

```powershell
pandoc docs/final/CHECKPOINT_1_PDF_READY.md -o docs/final/CHECKPOINT_1_PDF_READY.docx
```

o:

```powershell
pandoc docs/final/CHECKPOINT_1_PDF_READY.md -o docs/final/CHECKPOINT_1_PDF_READY.pdf
```

## 6. Checklist final antes de entregar

- confirmar que el documento indique `Segmento: S19`
- completar curso, seccion, docente, integrantes, fecha y URL del repositorio
- verificar que el flujo critico aparezca como:
  - `Inscripcion de estudiante a una seccion y generacion de cobro asociado`
- verificar que el PDF distinga claramente:
  - implementado ahora
  - preparado o configurado
  - pendiente o mejora futura
- no afirmar como cerrado lo siguiente si no hay evidencia local:
  - dashboards Grafana de negocio
  - scrapeo Prometheus de los microservicios
  - gateway operativo
  - frontend
  - automatizacion total entre inscripcion y cobro
- insertar capturas legibles de:
  - Redis
  - RabbitMQ
  - k6
  - Prometheus
  - Grafana
  - falla controlada
