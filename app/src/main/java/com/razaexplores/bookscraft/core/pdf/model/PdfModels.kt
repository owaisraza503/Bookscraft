package com.razaexplores.bookscraft.core.pdf.model

import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri

/**
 * Standard PDF Annotation types supported by Bookscraft.
 * Each type maps directly to a standard ISO 32000-1 PDF Annotation dictionary.
 */
enum class AnnotationType(val label: String, val pdfSubtype: String) {
    HIGHLIGHT("Highlight", "Highlight"),
    UNDERLINE("Underline", "Underline"),
    STRIKEOUT("Strikethrough", "StrikeOut"),
    STICKY_NOTE("Sticky Note", "Text"),
    INK("Freehand Pen", "Ink"),
    RECTANGLE("Rectangle", "Square"),
    CIRCLE("Circle", "Circle"),
    LINE("Line", "Line")
}

/**
 * Represents an annotation in Bookscraft.
 * Coordinates in [bounds] and [inkPoints] are stored in PDF page coordinate space (points, 72 dpi)
 * so they are invariant to screen resolution, zoom level, and display orientation.
 */
data class PdfAnnotationData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pageIndex: Int,
    val type: AnnotationType,
    val bounds: RectF, // In PDF coordinates (points)
    val color: Int = 0xFFFFEB3B.toInt(), // ARGB
    val opacity: Float = 0.85f,
    val contents: String? = null, // Sticky note content or highlight text
    val author: String? = "Bookscraft User",
    val strokeWidth: Float = 2.0f,
    val inkPoints: List<List<PointF>> = emptyList(), // Normalized or PDF coordinate paths
    val dateModified: Long = System.currentTimeMillis(),
    val isSavedInPdf: Boolean = false
)

/**
 * Metadata about an opened PDF document.
 */
data class PdfDocumentInfo(
    val uri: Uri? = null,
    val title: String,
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 0,
    val author: String? = null,
    val creator: String? = null,
    val producer: String? = null,
    val creationDate: String? = null,
    val isEncrypted: Boolean = false,
    val isEditable: Boolean = true,
    val hasAcroForm: Boolean = false
)

/**
 * Represents an AcroForm field found in the document.
 */
data class FormFieldItem(
    val name: String,
    val fullyQualifiedName: String,
    val type: FormFieldType,
    val currentValue: String,
    val pageIndex: Int = 0,
    val options: List<String> = emptyList(),
    val isReadOnly: Boolean = false
)

enum class FormFieldType {
    TEXT,
    CHECKBOX,
    RADIO_BUTTON,
    DROPDOWN,
    SIGNATURE,
    OTHER
}

/**
 * Text search occurrence inside the document.
 */
data class SearchMatch(
    val pageIndex: Int,
    val textSnippet: String,
    val matchIndexInPage: Int,
    val charStartIndex: Int,
    val charEndIndex: Int
)

/**
 * Page dimension and rotation info in PDF points.
 */
data class PageMetrics(
    val pageIndex: Int,
    val widthPts: Float,
    val heightPts: Float,
    val rotationDegrees: Int = 0
) {
    val aspectRatio: Float
        get() = if (heightPts > 0f) widthPts / heightPts else 1f
}
