package com.razaexplores.bookscraft.core.pdf.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.razaexplores.bookscraft.core.pdf.model.*
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionURI
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.*
import com.tom_roush.pdfbox.pdmodel.interactive.form.*
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class PdfBoxEngine : PdfEngine {

    companion object {
        private const val TAG = "PdfBoxEngine"
        @Volatile
        private var isInitialized = false
    }

    override fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    try {
                        PDFBoxResourceLoader.init(context.applicationContext)
                        isInitialized = true
                        Log.d(TAG, "PDFBox initialized successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize PDFBox", e)
                    }
                }
            }
        }
    }

    override fun getDocumentInfo(file: File): PdfDocumentInfo {
        if (!file.exists()) {
            return PdfDocumentInfo(title = file.name, fileSizeBytes = 0L)
        }
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(file)
            val info = doc.documentInformation
            val pageCount = doc.numberOfPages
            val acroForm = doc.documentCatalog.acroForm
            val isEncrypted = doc.isEncrypted

            PdfDocumentInfo(
                title = info?.title?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension,
                fileSizeBytes = file.length(),
                pageCount = pageCount,
                author = info?.author,
                creator = info?.creator,
                producer = info?.producer,
                creationDate = info?.creationDate?.time?.toString(),
                isEncrypted = isEncrypted,
                isEditable = !isEncrypted,
                hasAcroForm = acroForm != null && acroForm.fields.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document info", e)
            PdfDocumentInfo(
                title = file.nameWithoutExtension,
                fileSizeBytes = file.length(),
                pageCount = 0
            )
        } finally {
            doc?.close()
        }
    }

    override fun getPageCount(file: File): Int {
        if (!file.exists()) return 0
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(file)
            doc.numberOfPages
        } catch (e: Exception) {
            Log.e(TAG, "Error reading page count", e)
            0
        } finally {
            doc?.close()
        }
    }

    override fun getPageMetrics(file: File, pageIndex: Int): PageMetrics? {
        if (!file.exists()) return null
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(file)
            if (pageIndex in 0 until doc.numberOfPages) {
                val page = doc.getPage(pageIndex)
                val mediaBox = page.mediaBox ?: page.cropBox ?: PDRectangle.LETTER
                val rotation = page.rotation
                PageMetrics(
                    pageIndex = pageIndex,
                    widthPts = mediaBox.width,
                    heightPts = mediaBox.height,
                    rotationDegrees = rotation
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading page metrics", e)
            null
        } finally {
            doc?.close()
        }
    }

    /**
     * Renders a page using native Android PdfRenderer for optimal speed and sharpness.
     */
    override fun renderPage(file: File, pageIndex: Int, targetWidthPx: Int): Bitmap? {
        if (!file.exists() || targetWidthPx <= 0) return null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex !in 0 until renderer.pageCount) return null

            page = renderer.openPage(pageIndex)
            val srcWidth = page.width
            val srcHeight = page.height
            val scale = targetWidthPx.toFloat() / srcWidth.toFloat()
            val targetHeightPx = (srcHeight * scale).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering page $pageIndex", e)
            null
        } finally {
            page?.close()
            renderer?.close()
            pfd?.close()
        }
    }

    override fun extractText(file: File, pageIndex: Int): String {
        if (!file.exists()) return ""
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(file)
            if (pageIndex in 0 until doc.numberOfPages) {
                val stripper = PDFTextStripper()
                stripper.startPage = pageIndex + 1
                stripper.endPage = pageIndex + 1
                stripper.getText(doc) ?: ""
            } else ""
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from page $pageIndex", e)
            ""
        } finally {
            doc?.close()
        }
    }

    override fun searchText(file: File, query: String): List<SearchMatch> {
        if (!file.exists() || query.isBlank()) return emptyList()
        val results = mutableListOf<SearchMatch>()
        var doc: PDDocument? = null

        try {
            doc = PDDocument.load(file)
            val pageCount = doc.numberOfPages
            val stripper = PDFTextStripper()

            for (pageIdx in 0 until pageCount) {
                stripper.startPage = pageIdx + 1
                stripper.endPage = pageIdx + 1
                val text = stripper.getText(doc) ?: continue

                var startIndex = 0
                var matchCount = 0
                while (startIndex < text.length) {
                    val foundIdx = text.indexOf(query, startIndex, ignoreCase = true)
                    if (foundIdx == -1) break

                    // Generate a snippet with context around the match
                    val snippetStart = (foundIdx - 30).coerceAtLeast(0)
                    val snippetEnd = (foundIdx + query.length + 30).coerceAtMost(text.length)
                    val rawSnippet = text.substring(snippetStart, snippetEnd).replace("\n", " ").trim()
                    val prefix = if (snippetStart > 0) "…" else ""
                    val suffix = if (snippetEnd < text.length) "…" else ""

                    results.add(
                        SearchMatch(
                            pageIndex = pageIdx,
                            textSnippet = "$prefix$rawSnippet$suffix",
                            matchIndexInPage = matchCount++,
                            charStartIndex = foundIdx,
                            charEndIndex = foundIdx + query.length
                        )
                    )
                    startIndex = foundIdx + query.length
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching text for query: $query", e)
        } finally {
            doc?.close()
        }

        return results
    }

    override fun loadAnnotations(file: File): List<PdfAnnotationData> {
        if (!file.exists()) return emptyList()
        val annotationsList = mutableListOf<PdfAnnotationData>()
        var doc: PDDocument? = null

        try {
            doc = PDDocument.load(file)
            for (pageIdx in 0 until doc.numberOfPages) {
                val page = doc.getPage(pageIdx)
                val annots = page.annotations ?: continue

                for (annot in annots) {
                    val rect = annot.rectangle ?: continue
                    val bounds = RectF(rect.lowerLeftX, rect.lowerLeftY, rect.upperRightX, rect.upperRightY)

                    // Extract color
                    var colorInt = 0xFFFFEB3B.toInt()
                    val pdColor = annot.color
                    if (pdColor != null && pdColor.components != null && pdColor.components.size >= 3) {
                        val r = (pdColor.components[0] * 255f).toInt().coerceIn(0, 255)
                        val g = (pdColor.components[1] * 255f).toInt().coerceIn(0, 255)
                        val b = (pdColor.components[2] * 255f).toInt().coerceIn(0, 255)
                        colorInt = Color.rgb(r, g, b)
                    }

                    when {
                        annot is PDAnnotationTextMarkup -> {
                            val type = when (annot.subtype) {
                                PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT -> AnnotationType.HIGHLIGHT
                                PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE -> AnnotationType.UNDERLINE
                                PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT -> AnnotationType.STRIKEOUT
                                else -> AnnotationType.HIGHLIGHT
                            }
                            annotationsList.add(
                                PdfAnnotationData(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = pageIdx,
                                    type = type,
                                    bounds = bounds,
                                    color = colorInt,
                                    contents = annot.contents,
                                    author = (annot as? PDAnnotationMarkup)?.titlePopup,
                                    isSavedInPdf = true
                                )
                            )
                        }
                        annot is PDAnnotationText -> {
                            annotationsList.add(
                                PdfAnnotationData(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = pageIdx,
                                    type = AnnotationType.STICKY_NOTE,
                                    bounds = bounds,
                                    color = colorInt,
                                    contents = annot.contents,
                                    author = annot.titlePopup,
                                    isSavedInPdf = true
                                )
                            )
                        }
                        annot is PDAnnotationSquareCircle -> {
                            val type = if (annot.subtype == PDAnnotationSquareCircle.SUB_TYPE_CIRCLE) {
                                AnnotationType.CIRCLE
                            } else {
                                AnnotationType.RECTANGLE
                            }
                            annotationsList.add(
                                PdfAnnotationData(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = pageIdx,
                                    type = type,
                                    bounds = bounds,
                                    color = colorInt,
                                    contents = annot.contents,
                                    isSavedInPdf = true
                                )
                            )
                        }
                        annot is PDAnnotationLine -> {
                            annotationsList.add(
                                PdfAnnotationData(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = pageIdx,
                                    type = AnnotationType.LINE,
                                    bounds = bounds,
                                    color = colorInt,
                                    contents = annot.contents,
                                    isSavedInPdf = true
                                )
                            )
                        }
                        annot is PDAnnotationMarkup && annot.subtype == PDAnnotationMarkup.SUB_TYPE_INK -> {
                            val inkList = annot.inkList
                            val paths = mutableListOf<List<PointF>>()
                            if (inkList != null) {
                                for (pathFloats in inkList) {
                                    val points = mutableListOf<PointF>()
                                    var i = 0
                                    while (i + 1 < pathFloats.size) {
                                        points.add(PointF(pathFloats[i], pathFloats[i + 1]))
                                        i += 2
                                    }
                                    if (points.isNotEmpty()) {
                                        paths.add(points)
                                    }
                                }
                            }
                            annotationsList.add(
                                PdfAnnotationData(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = pageIdx,
                                    type = AnnotationType.INK,
                                    bounds = bounds,
                                    color = colorInt,
                                    inkPoints = paths,
                                    isSavedInPdf = true
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading annotations", e)
        } finally {
            doc?.close()
        }

        return annotationsList
    }

    override fun saveAnnotations(
        sourceFile: File,
        outputFile: File,
        annotationsToAdd: List<PdfAnnotationData>,
        annotationIdsToDelete: Set<String>
    ): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            val pageCount = doc.numberOfPages

            for (annot in annotationsToAdd) {
                if (annot.pageIndex !in 0 until pageCount) continue
                val page = doc.getPage(annot.pageIndex)

                val r = Color.red(annot.color) / 255f
                val g = Color.green(annot.color) / 255f
                val b = Color.blue(annot.color) / 255f
                val pdColor = PDColor(floatArrayOf(r, g, b), PDDeviceRGB.INSTANCE)

                val pdfRect = PDRectangle(
                    annot.bounds.left,
                    annot.bounds.top,
                    annot.bounds.width().coerceAtLeast(10f),
                    annot.bounds.height().coerceAtLeast(10f)
                )

                when (annot.type) {
                    AnnotationType.HIGHLIGHT -> {
                        val hl = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT)
                        hl.rectangle = pdfRect
                        hl.color = pdColor
                        hl.contents = annot.contents ?: "Highlight"
                        hl.constantOpacity = annot.opacity
                        hl.quadPoints = floatArrayOf(
                            annot.bounds.left, annot.bounds.bottom,
                            annot.bounds.right, annot.bounds.bottom,
                            annot.bounds.left, annot.bounds.top,
                            annot.bounds.right, annot.bounds.top
                        )
                        page.annotations.add(hl)
                    }
                    AnnotationType.UNDERLINE -> {
                        val ul = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE)
                        ul.rectangle = pdfRect
                        ul.color = pdColor
                        ul.contents = annot.contents ?: "Underline"
                        ul.quadPoints = floatArrayOf(
                            annot.bounds.left, annot.bounds.bottom,
                            annot.bounds.right, annot.bounds.bottom,
                            annot.bounds.left, annot.bounds.top,
                            annot.bounds.right, annot.bounds.top
                        )
                        page.annotations.add(ul)
                    }
                    AnnotationType.STRIKEOUT -> {
                        val so = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT)
                        so.rectangle = pdfRect
                        so.color = pdColor
                        so.contents = annot.contents ?: "Strikethrough"
                        so.quadPoints = floatArrayOf(
                            annot.bounds.left, annot.bounds.bottom,
                            annot.bounds.right, annot.bounds.bottom,
                            annot.bounds.left, annot.bounds.top,
                            annot.bounds.right, annot.bounds.top
                        )
                        page.annotations.add(so)
                    }
                    AnnotationType.STICKY_NOTE -> {
                        val note = PDAnnotationText()
                        note.rectangle = PDRectangle(annot.bounds.left, annot.bounds.top, 24f, 24f)
                        note.contents = annot.contents ?: ""
                        note.titlePopup = annot.author ?: "Bookscraft User"
                        note.color = pdColor
                        note.name = PDAnnotationText.NAME_COMMENT
                        page.annotations.add(note)
                    }
                    AnnotationType.INK -> {
                        val ink = PDAnnotationMarkup()
                        ink.cosObject.setName(COSName.SUBTYPE, PDAnnotationMarkup.SUB_TYPE_INK)
                        ink.rectangle = pdfRect
                        ink.color = pdColor
                        ink.constantOpacity = annot.opacity

                        val pathArrays = mutableListOf<FloatArray>()
                        for (path in annot.inkPoints) {
                            val floatArr = FloatArray(path.size * 2)
                            var idx = 0
                            for (pt in path) {
                                floatArr[idx++] = pt.x
                                floatArr[idx++] = pt.y
                            }
                            pathArrays.add(floatArr)
                        }
                        if (pathArrays.isNotEmpty()) {
                            ink.inkList = pathArrays.toTypedArray()
                        }
                        page.annotations.add(ink)
                    }
                    AnnotationType.RECTANGLE -> {
                        val square = PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE)
                        square.rectangle = pdfRect
                        square.color = pdColor
                        square.contents = annot.contents
                        page.annotations.add(square)
                    }
                    AnnotationType.CIRCLE -> {
                        val circle = PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_CIRCLE)
                        circle.rectangle = pdfRect
                        circle.color = pdColor
                        circle.contents = annot.contents
                        page.annotations.add(circle)
                    }
                    AnnotationType.LINE -> {
                        val line = PDAnnotationLine()
                        line.rectangle = pdfRect
                        line.line = floatArrayOf(
                            annot.bounds.left, annot.bounds.top,
                            annot.bounds.right, annot.bounds.bottom
                        )
                        line.color = pdColor
                        page.annotations.add(line)
                    }
                }
            }

            // Save to destination
            val tempFile = File(outputFile.parentFile, "tmp_${System.currentTimeMillis()}_${outputFile.name}")
            doc.save(tempFile)
            doc.close()
            doc = null

            // Safely atomic replace
            if (outputFile.exists()) {
                outputFile.delete()
            }
            tempFile.renameTo(outputFile)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving annotations to PDF", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }

    override fun rotatePage(sourceFile: File, outputFile: File, pageIndex: Int, degreesDelta: Int): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            if (pageIndex in 0 until doc.numberOfPages) {
                val page = doc.getPage(pageIndex)
                val newRot = (page.rotation + degreesDelta) % 360
                page.rotation = if (newRot < 0) newRot + 360 else newRot
                doc.save(outputFile)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Page index $pageIndex out of range"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating page", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }

    override fun deletePages(sourceFile: File, outputFile: File, pageIndices: List<Int>): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            val sortedIndices = pageIndices.sortedDescending()
            for (idx in sortedIndices) {
                if (idx in 0 until doc.numberOfPages && doc.numberOfPages > 1) {
                    doc.removePage(idx)
                }
            }
            doc.save(outputFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pages", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }

    override fun reorderPage(sourceFile: File, outputFile: File, fromIndex: Int, toIndex: Int): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            val count = doc.numberOfPages
            if (fromIndex in 0 until count && toIndex in 0 until count && fromIndex != toIndex) {
                val page = doc.getPage(fromIndex)
                doc.removePage(fromIndex)
                // Insert at new index
                val newDoc = PDDocument()
                for (i in 0 until doc.numberOfPages) {
                    if (i == toIndex) {
                        newDoc.addPage(page)
                    }
                    newDoc.addPage(doc.getPage(i))
                }
                if (toIndex >= doc.numberOfPages) {
                    newDoc.addPage(page)
                }
                newDoc.save(outputFile)
                newDoc.close()
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Invalid reorder indices"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reordering pages", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }

    override fun extractPages(sourceFile: File, outputFile: File, pageIndices: List<Int>): Result<Unit> {
        var srcDoc: PDDocument? = null
        var newDoc: PDDocument? = null
        return try {
            srcDoc = PDDocument.load(sourceFile)
            newDoc = PDDocument()
            for (idx in pageIndices.sorted()) {
                if (idx in 0 until srcDoc.numberOfPages) {
                    newDoc.addPage(srcDoc.getPage(idx))
                }
            }
            newDoc.save(outputFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting pages", e)
            Result.failure(e)
        } finally {
            srcDoc?.close()
            newDoc?.close()
        }
    }

    override fun mergeDocuments(sourceFiles: List<File>, outputFile: File): Result<Unit> {
        val mergedDoc = PDDocument()
        return try {
            for (file in sourceFiles) {
                if (!file.exists()) continue
                val doc = PDDocument.load(file)
                for (i in 0 until doc.numberOfPages) {
                    mergedDoc.addPage(doc.getPage(i))
                }
                doc.close()
            }
            mergedDoc.save(outputFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error merging documents", e)
            Result.failure(e)
        } finally {
            mergedDoc.close()
        }
    }

    override fun getFormFields(file: File): List<FormFieldItem> {
        if (!file.exists()) return emptyList()
        val formFields = mutableListOf<FormFieldItem>()
        var doc: PDDocument? = null

        try {
            doc = PDDocument.load(file)
            val acroForm = doc.documentCatalog.acroForm ?: return emptyList()

            for (field in acroForm.fields) {
                val fieldType = when (field) {
                    is PDTextField -> FormFieldType.TEXT
                    is PDCheckBox -> FormFieldType.CHECKBOX
                    is PDRadioButton -> FormFieldType.RADIO_BUTTON
                    is PDComboBox -> FormFieldType.DROPDOWN
                    is PDSignatureField -> FormFieldType.SIGNATURE
                    else -> FormFieldType.OTHER
                }

                val value = try {
                    field.valueAsString ?: ""
                } catch (e: Exception) {
                    ""
                }

                formFields.add(
                    FormFieldItem(
                        name = field.partialName ?: "Field",
                        fullyQualifiedName = field.fullyQualifiedName ?: "Field",
                        type = fieldType,
                        currentValue = value,
                        isReadOnly = field.isReadOnly
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting form fields", e)
        } finally {
            doc?.close()
        }

        return formFields
    }

    override fun fillFormFields(sourceFile: File, outputFile: File, fieldValues: Map<String, String>): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            val acroForm = doc.documentCatalog.acroForm ?: return Result.failure(IllegalStateException("No AcroForm in document"))

            for ((fieldName, value) in fieldValues) {
                val field = acroForm.getField(fieldName)
                if (field != null) {
                    try {
                        field.setValue(value)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to set field value for $fieldName: $value", e)
                    }
                }
            }

            doc.save(outputFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error filling form fields", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }

    override fun updateDocumentMetadata(
        sourceFile: File,
        outputFile: File,
        title: String?,
        author: String?,
        subject: String?,
        keywords: String?
    ): Result<Unit> {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(sourceFile)
            val info = doc.documentInformation
            if (title != null) info.title = title
            if (author != null) info.author = author
            if (subject != null) info.subject = subject
            if (keywords != null) info.keywords = keywords
            info.modificationDate = Calendar.getInstance()

            doc.save(outputFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating metadata", e)
            Result.failure(e)
        } finally {
            doc?.close()
        }
    }
}
