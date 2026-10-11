# Bookscraft Architecture

## Architecture Overview

Bookscraft follows Android's recommended Clean Architecture pattern with MVVM (Model-View-ViewModel):

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (LibraryScreen, ViewerScreen, Organize, Forms)       │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                  PdfViewerViewModel                    │
│   (Document state, Zoom/Pan, Active tool, Undo/Redo)   │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
              ▼                            ▼
┌───────────────────────────┐┌───────────────────────────┐
│     PdfBoxEngine          ││   BookscraftDatabase      │
│   (PdfEngine Interface)   ││   (Room SQLite)           │
│  - Android PdfRenderer    ││  - Recent documents       │
│  - Apache PDFBox Android  ││  - Reading bookmarks      │
└───────────────────────────┘└───────────────────────────┘
```

## Coordinate System Transformation

PDF coordinates use a lower-left origin `(0, 0)` with units measured in 72 points per inch (points).
Android touch events and View coordinates use a top-left origin `(0, 0)` measured in screen pixels.

### Touch to PDF Points:
```kotlin
val pdfX = (touchX / viewWidthPx) * pageWidthPts
val pdfY = (1f - (touchY / viewHeightPx)) * pageHeightPts
```

### PDF Points to Canvas Rendering:
```kotlin
val canvasX = (pdfX / pageWidthPts) * viewWidthPx
val canvasY = (1f - (pdfY / pageHeightPts)) * viewHeightPx
```

This ensures annotations remain precisely positioned regardless of screen density, zoom level, or orientation.
