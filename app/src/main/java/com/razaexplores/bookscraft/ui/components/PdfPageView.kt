package com.razaexplores.bookscraft.ui.components

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.core.pdf.model.AnnotationType
import com.razaexplores.bookscraft.core.pdf.model.PdfAnnotationData
import com.razaexplores.bookscraft.ui.viewmodel.ViewerTool

@Composable
fun PdfPageView(
    pageIndex: Int,
    bitmap: Bitmap?,
    annotations: List<PdfAnnotationData>,
    activeTool: ViewerTool,
    selectedColor: Int,
    strokeWidth: Float,
    isNightMode: Boolean,
    onAddAnnotation: (PdfAnnotationData) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    onStickyNoteClick: (PdfAnnotationData) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // In-progress drawing states
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var currentDragOffset by remember { mutableStateOf<Offset?>(null) }
    val currentPenPath = remember { mutableStateListOf<Offset>() }

    // Night mode color matrix: invert colors while preserving readability
    val nightModeFilter = remember {
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        ColorFilter.colorMatrix(matrix)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .shadow(4.dp, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .onSizeChanged { viewSize = it }
            .pointerInput(activeTool, viewSize, selectedColor) {
                if (viewSize.width == 0 || viewSize.height == 0) return@pointerInput

                // Handle tool-specific gestures
                when (activeTool) {
                    ViewerTool.VIEW_PAN -> {
                        // Taps in view mode can open sticky notes
                        detectTapGestures { tapOffset ->
                            val tappedNote = annotations.firstOrNull { annot ->
                                if (annot.type == AnnotationType.STICKY_NOTE) {
                                    val noteLeft = (annot.bounds.left / 612f) * viewSize.width
                                    val noteTop = (1f - (annot.bounds.top / 792f)) * viewSize.height
                                    val dist = (tapOffset.x - noteLeft) * (tapOffset.x - noteLeft) +
                                            (tapOffset.y - noteTop) * (tapOffset.y - noteTop)
                                    dist < 40 * 40
                                } else false
                            }
                            if (tappedNote != null) {
                                onStickyNoteClick(tappedNote)
                            }
                        }
                    }

                    ViewerTool.STICKY_NOTE -> {
                        detectTapGestures { tapOffset ->
                            // Convert touch to PDF coordinates (assuming standard Letter 612x792 pts)
                            val pdfX = (tapOffset.x / viewSize.width) * 612f
                            val pdfY = (1f - (tapOffset.y / viewSize.height)) * 792f

                            val newNote = PdfAnnotationData(
                                pageIndex = pageIndex,
                                type = AnnotationType.STICKY_NOTE,
                                bounds = RectF(pdfX, pdfY, pdfX + 24f, pdfY + 24f),
                                color = selectedColor,
                                contents = "New note",
                                isSavedInPdf = false
                            )
                            onAddAnnotation(newNote)
                            onStickyNoteClick(newNote)
                        }
                    }

                    ViewerTool.ERASER -> {
                        detectTapGestures { tapOffset ->
                            val clickedAnnot = annotations.firstOrNull { annot ->
                                val left = (annot.bounds.left / 612f) * viewSize.width
                                val right = (annot.bounds.right / 612f) * viewSize.width
                                val top = (1f - (annot.bounds.top / 792f)) * viewSize.height
                                val bottom = (1f - (annot.bounds.bottom / 792f)) * viewSize.height

                                val minX = minOf(left, right) - 10f
                                val maxX = maxOf(left, right) + 10f
                                val minY = minOf(top, bottom) - 10f
                                val maxY = maxOf(top, bottom) + 10f

                                tapOffset.x in minX..maxX && tapOffset.y in minY..maxY
                            }
                            if (clickedAnnot != null) {
                                onDeleteAnnotation(clickedAnnot.id)
                            }
                        }
                    }

                    ViewerTool.PEN -> {
                        detectDragGestures(
                            onDragStart = { start ->
                                currentPenPath.clear()
                                currentPenPath.add(start)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPenPath.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPenPath.size >= 2) {
                                    val points = currentPenPath.map { pt ->
                                        val pX = (pt.x / viewSize.width) * 612f
                                        val pY = (1f - (pt.y / viewSize.height)) * 792f
                                        PointF(pX, pY)
                                    }
                                    val minX = points.minOf { it.x }
                                    val maxX = points.maxOf { it.x }
                                    val minY = points.minOf { it.y }
                                    val maxY = points.maxOf { it.y }

                                    val inkAnnot = PdfAnnotationData(
                                        pageIndex = pageIndex,
                                        type = AnnotationType.INK,
                                        bounds = RectF(minX, minY, maxX, maxY),
                                        color = selectedColor,
                                        strokeWidth = strokeWidth,
                                        inkPoints = listOf(points),
                                        isSavedInPdf = false
                                    )
                                    onAddAnnotation(inkAnnot)
                                }
                                currentPenPath.clear()
                            },
                            onDragCancel = {
                                currentPenPath.clear()
                            }
                        )
                    }

                    ViewerTool.HIGHLIGHT, ViewerTool.UNDERLINE, ViewerTool.STRIKEOUT,
                    ViewerTool.RECTANGLE, ViewerTool.CIRCLE -> {
                        detectDragGestures(
                            onDragStart = { start ->
                                dragStartOffset = start
                                currentDragOffset = start
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentDragOffset = change.position
                            },
                            onDragEnd = {
                                val start = dragStartOffset
                                val end = currentDragOffset
                                if (start != null && end != null) {
                                    val leftPx = minOf(start.x, end.x)
                                    val rightPx = maxOf(start.x, end.x)
                                    val topPx = minOf(start.y, end.y)
                                    val bottomPx = maxOf(start.y, end.y)

                                    if (rightPx - leftPx > 5f || bottomPx - topPx > 5f) {
                                        val pdfLeft = (leftPx / viewSize.width) * 612f
                                        val pdfRight = (rightPx / viewSize.width) * 612f
                                        val pdfTop = (1f - (topPx / viewSize.height)) * 792f
                                        val pdfBottom = (1f - (bottomPx / viewSize.height)) * 792f

                                        val annotType = when (activeTool) {
                                            ViewerTool.HIGHLIGHT -> AnnotationType.HIGHLIGHT
                                            ViewerTool.UNDERLINE -> AnnotationType.UNDERLINE
                                            ViewerTool.STRIKEOUT -> AnnotationType.STRIKEOUT
                                            ViewerTool.RECTANGLE -> AnnotationType.RECTANGLE
                                            ViewerTool.CIRCLE -> AnnotationType.CIRCLE
                                            else -> AnnotationType.HIGHLIGHT
                                        }

                                        val newAnnot = PdfAnnotationData(
                                            pageIndex = pageIndex,
                                            type = annotType,
                                            bounds = RectF(
                                                minOf(pdfLeft, pdfRight),
                                                minOf(pdfTop, pdfBottom),
                                                maxOf(pdfLeft, pdfRight),
                                                maxOf(pdfTop, pdfBottom)
                                            ),
                                            color = selectedColor,
                                            strokeWidth = strokeWidth,
                                            opacity = if (annotType == AnnotationType.HIGHLIGHT) 0.45f else 0.85f,
                                            isSavedInPdf = false
                                        )
                                        onAddAnnotation(newAnnot)
                                    }
                                }
                                dragStartOffset = null
                                currentDragOffset = null
                            },
                            onDragCancel = {
                                dragStartOffset = null
                                currentDragOffset = null
                            }
                        )
                    }
                }
            }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.FillWidth,
                colorFilter = if (isNightMode) nightModeFilter else null,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .background(Color.White)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
        }

        // Overlay Canvas for Drawing Annotations
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw existing annotations
            for (annot in annotations) {
                val color = Color(annot.color).copy(alpha = annot.opacity)
                val left = (annot.bounds.left / 612f) * width
                val right = (annot.bounds.right / 612f) * width
                val top = (1f - (annot.bounds.top / 792f)) * height
                val bottom = (1f - (annot.bounds.bottom / 792f)) * height

                val rectX = minOf(left, right)
                val rectY = minOf(top, bottom)
                val rectW = (maxOf(left, right) - rectX).coerceAtLeast(4f)
                val rectH = (maxOf(top, bottom) - rectY).coerceAtLeast(4f)

                when (annot.type) {
                    AnnotationType.HIGHLIGHT -> {
                        drawRect(
                            color = color.copy(alpha = 0.35f),
                            topLeft = Offset(rectX, rectY),
                            size = Size(rectW, rectH)
                        )
                    }
                    AnnotationType.UNDERLINE -> {
                        drawLine(
                            color = color.copy(alpha = 0.9f),
                            start = Offset(rectX, rectY + rectH),
                            end = Offset(rectX + rectW, rectY + rectH),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                    AnnotationType.STRIKEOUT -> {
                        drawLine(
                            color = color.copy(alpha = 0.9f),
                            start = Offset(rectX, rectY + rectH / 2f),
                            end = Offset(rectX + rectW, rectY + rectH / 2f),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }
                    AnnotationType.RECTANGLE -> {
                        drawRect(
                            color = color,
                            topLeft = Offset(rectX, rectY),
                            size = Size(rectW, rectH),
                            style = Stroke(width = 3f)
                        )
                    }
                    AnnotationType.CIRCLE -> {
                        drawOval(
                            color = color,
                            topLeft = Offset(rectX, rectY),
                            size = Size(rectW, rectH),
                            style = Stroke(width = 3f)
                        )
                    }
                    AnnotationType.INK -> {
                        for (path in annot.inkPoints) {
                            if (path.size >= 2) {
                                val composePath = Path()
                                val first = path[0]
                                composePath.moveTo(
                                    (first.x / 612f) * width,
                                    (1f - (first.y / 792f)) * height
                                )
                                for (i in 1 until path.size) {
                                    val pt = path[i]
                                    composePath.lineTo(
                                        (pt.x / 612f) * width,
                                        (1f - (pt.y / 792f)) * height
                                    )
                                }
                                drawPath(
                                    path = composePath,
                                    color = color,
                                    style = Stroke(
                                        width = annot.strokeWidth.coerceAtLeast(2f),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                    AnnotationType.STICKY_NOTE -> {
                        // Draw sticky note icon badge
                        drawCircle(
                            color = Color(annot.color),
                            radius = 14f,
                            center = Offset(rectX + 12f, rectY + 12f)
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.3f),
                            radius = 14f,
                            center = Offset(rectX + 12f, rectY + 12f),
                            style = Stroke(width = 1.5f)
                        )
                    }
                    else -> {}
                }
            }

            // 2. Draw in-progress Drag Selection
            if (dragStartOffset != null && currentDragOffset != null) {
                val start = dragStartOffset!!
                val end = currentDragOffset!!
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val w = maxOf(start.x, end.x) - left
                val h = maxOf(start.y, end.y) - top

                val previewColor = Color(selectedColor)
                when (activeTool) {
                    ViewerTool.HIGHLIGHT -> {
                        drawRect(
                            color = previewColor.copy(alpha = 0.35f),
                            topLeft = Offset(left, top),
                            size = Size(w, h)
                        )
                    }
                    ViewerTool.RECTANGLE -> {
                        drawRect(
                            color = previewColor,
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            style = Stroke(width = 2.5f)
                        )
                    }
                    ViewerTool.CIRCLE -> {
                        drawOval(
                            color = previewColor,
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            style = Stroke(width = 2.5f)
                        )
                    }
                    ViewerTool.UNDERLINE -> {
                        drawLine(
                            color = previewColor,
                            start = Offset(left, top + h),
                            end = Offset(left + w, top + h),
                            strokeWidth = 3f
                        )
                    }
                    ViewerTool.STRIKEOUT -> {
                        drawLine(
                            color = previewColor,
                            start = Offset(left, top + h / 2f),
                            end = Offset(left + w, top + h / 2f),
                            strokeWidth = 2.5f
                        )
                    }
                    else -> {}
                }
            }

            // 3. Draw in-progress Pen Path
            if (currentPenPath.size >= 2) {
                val path = Path()
                path.moveTo(currentPenPath[0].x, currentPenPath[0].y)
                for (i in 1 until currentPenPath.size) {
                    path.lineTo(currentPenPath[i].x, currentPenPath[i].y)
                }
                drawPath(
                    path = path,
                    color = Color(selectedColor),
                    style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}
