# Bookscraft 📖✍️

> **Free, open-source, offline-first PDF reader, annotator, editor, and organizer for Android phones and tablets.**
> Built with Kotlin, Jetpack Compose, Material 3, and Apache PDFBox.

---

## 🌟 Mission & Core Principles

Bookscraft is created on a simple premise: **users deserve a powerful, professional mobile PDF application without paywalls, subscriptions, advertisements, forced accounts, or telemetry.**

1. **100% Free & Open Source:** Licensed under the Apache License 2.0.
2. **Permanent Embedded PDF Annotations:** Every highlight, sticky note, underline, strikethrough, freehand ink stroke, and shape is saved as an **official ISO 32000-1 PDF annotation object** (`/Highlight`, `/Text`, `/Underline`, `/StrikeOut`, `/Ink`, `/Square`, `/Circle`). Your annotations appear in Adobe Acrobat, Google Chrome, Apple Preview, and every standards-compliant PDF viewer.
3. **Completely Offline & Private:** Zero tracking, zero telemetry, no network permissions required. Your documents never leave your device.
4. **Native Android Performance:** High-performance hardware-accelerated page rendering powered by native Android `PdfRenderer` and structural PDF manipulation via Apache PDFBox Android.
5. **Modern Material 3 Design:** Dynamic color palettes, adaptive phone and tablet layouts, and fluid gestures.

---

## 📱 Application Identity

- **Product Name:** Bookscraft
- **Application ID:** `com.razaexplores.bookscraft`
- **Minimum SDK:** Android API 26 (Android 8.0 Oreo)
- **Target SDK:** Android API 36 (Android 15+)
- **Architecture:** Clean MVVM with Coroutines & StateFlow

---

## 🛠 Features

### 1. Document Reading & Navigation
- **Dual Reading Modes:** Continuous vertical scrolling or single-page view.
- **Pinch-to-Zoom & Pan:** Smooth, responsive scaling up to 5x.
- **Night / Inverted Mode:** Comfortable reading filter for low-light conditions.
- **Full Document Text Search:** Fast, case-insensitive keyword search with snippet previews and next/previous match jumping.
- **Page Thumbnails Grid:** Interactive page drawer to jump quickly to any page.
- **Recent Documents & Favorites:** Room-persisted reading history with page progress.

### 2. Embedded Annotation Toolkit
- **Text Highlighting:** Multi-color highlighter (Yellow, Green, Pink, Cyan, Orange, Purple) with realistic opacity.
- **Text Underline & Strikethrough:** Standard typographic markup.
- **Sticky Notes / Comments:** Tap anywhere on a page to insert a portable comment with author, date, and expandable message dialog.
- **Freehand Pen & Highlighter:** Smooth vector stylus/finger drawing with configurable stroke thickness.
- **Geometric Shapes:** Rectangles and circles for diagrams and flowcharts.
- **Eraser & Undo/Redo:** Instant touch deletion of annotations and full edit history stack.
- **Direct Save & Export:** Save in-place or export an annotated copy through Android Storage Access Framework (SAF).

### 3. Page Organization & Editing
- **Rotate Pages:** 90° clockwise and counter-clockwise rotation written to the PDF structure.
- **Delete Pages:** Remove unwanted pages with confirmation.
- **Extract Pages:** Save selected pages to a new PDF document.
- **Document Properties:** View and edit PDF metadata (Title, Author, Subject, Keywords).

### 4. Interactive Forms
- **AcroForm Field Detection:** Automatically scans fillable form fields (`PDTextField`, `PDCheckBox`).
- **In-App Form Filling:** Edit values directly and commit them back into the PDF binary.

---

## 🧪 Annotation Round-Trip Verification

Bookscraft guarantees portable annotations. The test suite includes automated round-trip tests (`ExampleUnitTest.kt`):
1. Creates a real PDF document.
2. Embeds standard `/Highlight`, `/Text` (sticky note), `/Underline`, `/Ink`, and `/Square` objects.
3. Serializes to binary output stream.
4. Closes the document and reloads it from raw bytes.
5. Inspects each annotation dictionary, validating subtype, bounding box, color components, and content strings.

Run the test suite with:
```bash
gradle :app:testDebugUnitTest
```

---

## 🏗 Building the Project

### Prerequisites
- JDK 17 or JDK 21
- Android SDK with Platform 36 and Build-Tools
- Gradle 9+ (or Android Studio Ladybug / Meerkat)

### Build Commands
```bash
# Run unit tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug

# Assemble Release APK
gradle :app:assembleRelease
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

```
Copyright 2026 Raza Explores

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
