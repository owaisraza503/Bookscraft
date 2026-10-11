# PDF Engine Capabilities & Specification

## Engine Selection

Bookscraft integrates two synergistic engines:

1. **Android `PdfRenderer` (System Framework API 26+)**:
   - Hardware-accelerated native Skia rasterization.
   - Used for zero-latency, high-resolution bitmap page rendering.
   - Low memory footprint: renders only currently visible pages on-demand.

2. **Apache PDFBox for Android (`com.tom-roush:pdfbox-android`)**:
   - Pure Java ISO 32000-1 document model parser and writer.
   - Apache-2.0 license compatible.
   - Creates and parses real PDF `/Annots` dictionaries.
   - Handles page tree operations, text stripping, and AcroForms.

## Supported Annotation Types

| Type | PDF Subtype | Embedded Object | Properties Preserved |
| ---- | ----------- | --------------- | -------------------- |
| Text Highlight | `/Highlight` | `PDAnnotationTextMarkup` | Color, Opacity, QuadPoints, Contents |
| Underline | `/Underline` | `PDAnnotationTextMarkup` | Color, QuadPoints, Contents |
| Strikethrough | `/StrikeOut` | `PDAnnotationTextMarkup` | Color, QuadPoints, Contents |
| Sticky Note | `/Text` | `PDAnnotationText` | Color, Contents, Author, Date, Rect |
| Freehand Pen | `/Ink` | `PDAnnotationMarkup` | Color, Opacity, InkList paths, Rect |
| Rectangle | `/Square` | `PDAnnotationSquareCircle` | Color, Border, Rect |
| Circle | `/Circle` | `PDAnnotationSquareCircle` | Color, Border, Rect |

## Limitations & Honest Reporting

- **XFA Forms:** Dynamic XML Forms Architecture (XFA) is not supported by standard PDFBox; static AcroForms are supported.
- **Digital Signatures:** Visual signatures and freehand ink are supported; cryptographic PKCS#7 certificate hardware signing is planned for future phases.
- **OCR:** Scanned image-only documents without text layers cannot be searched until the optional on-device ML Kit OCR module is activated.
