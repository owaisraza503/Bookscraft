package com.razaexplores.bookscraft.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razaexplores.bookscraft.core.pdf.engine.PdfEngine
import com.razaexplores.bookscraft.core.pdf.model.*
import com.razaexplores.bookscraft.core.pdf.sample.SamplePdfGenerator
import com.razaexplores.bookscraft.data.local.BookscraftDatabase
import com.razaexplores.bookscraft.data.model.RecentDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ViewerTool {
    VIEW_PAN,
    HIGHLIGHT,
    UNDERLINE,
    STRIKEOUT,
    STICKY_NOTE,
    PEN,
    RECTANGLE,
    CIRCLE,
    ERASER
}

data class ViewerUiState(
    val currentFile: File? = null,
    val sourceUri: Uri? = null,
    val documentInfo: PdfDocumentInfo? = null,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val zoomLevel: Float = 1.0f,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val activeTool: ViewerTool = ViewerTool.VIEW_PAN,
    val selectedColor: Int = 0xFFFFEB3B.toInt(), // Default Yellow
    val strokeWidth: Float = 3.0f,
    val annotations: List<PdfAnnotationData> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<SearchMatch> = emptyList(),
    val currentSearchIndex: Int = -1,
    val isNightMode: Boolean = false,
    val isContinuousScroll: Boolean = true,
    val activeStickyNoteDialog: PdfAnnotationData? = null,
    val isInfoDialogOpen: Boolean = false,
    val formFields: List<FormFieldItem> = emptyList(),
    val statusMessage: String? = null
)

class PdfViewerViewModel(
    private val pdfEngine: PdfEngine,
    private val database: BookscraftDatabase
) : ViewModel() {

    private val TAG = "PdfViewerViewModel"

    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    val recentDocuments = database.recentDocumentDao().getAllRecentDocuments()

    // Page Bitmap Memory Cache (stores rendered pages for smooth scrolling and instant navigation)
    private val bitmapCache = object : LruCache<String, Bitmap>(32 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    private val undoStack = mutableListOf<List<PdfAnnotationData>>()
    private val redoStack = mutableListOf<List<PdfAnnotationData>>()

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun openSampleDocument(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val sampleFile = withContext(Dispatchers.IO) {
                pdfEngine.initialize(context)
                SamplePdfGenerator.getOrCreateSamplePdf(context)
            }
            openFile(context, sampleFile, null, "Bookscraft_Welcome_Guide.pdf")
        }
    }

    fun openUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                try {
                    pdfEngine.initialize(context)

                    var displayName = "Document.pdf"
                    var fileSize = 0L

                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIndex != -1) {
                                displayName = cursor.getString(nameIndex) ?: displayName
                            }
                            if (sizeIndex != -1) {
                                fileSize = cursor.getLong(sizeIndex)
                            }
                        }
                    }

                    // Copy to internal working directory
                    val workingDir = File(context.filesDir, "active_pdfs")
                    if (!workingDir.exists()) workingDir.mkdirs()

                    val targetFile = File(workingDir, "doc_${System.currentTimeMillis()}_$displayName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        openFile(context, targetFile, uri, displayName)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open document from URI: $uri", e)
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Could not open document: ${e.localizedMessage}"
                        )
                    }
                }
            }
        }
    }

    fun openFile(context: Context, file: File, sourceUri: Uri?, displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bitmapCache.evictAll()
            undoStack.clear()
            redoStack.clear()

            withContext(Dispatchers.IO) {
                pdfEngine.initialize(context)
                val info = pdfEngine.getDocumentInfo(file)
                val pageCount = pdfEngine.getPageCount(file)
                val existingAnnots = pdfEngine.loadAnnotations(file)
                val forms = pdfEngine.getFormFields(file)

                // Save to recent documents
                val recentDoc = RecentDocument(
                    uriString = sourceUri?.toString() ?: file.absolutePath,
                    fileName = displayName,
                    filePath = file.absolutePath,
                    fileSizeBytes = file.length(),
                    pageCount = pageCount,
                    lastOpenedTimestamp = System.currentTimeMillis()
                )
                database.recentDocumentDao().insertOrUpdate(recentDoc)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        currentFile = file,
                        sourceUri = sourceUri,
                        documentInfo = info,
                        pageCount = pageCount,
                        currentPage = 0,
                        zoomLevel = 1.0f,
                        annotations = existingAnnots,
                        formFields = forms,
                        isLoading = false,
                        hasUnsavedChanges = false,
                        statusMessage = "Opened $displayName ($pageCount pages)"
                    )
                }
            }
        }
    }

    fun getPageBitmap(pageIndex: Int, targetWidthPx: Int, onResult: (Bitmap?) -> Unit) {
        val file = _uiState.value.currentFile ?: return
        val cacheKey = "${file.absolutePath}_${pageIndex}_$targetWidthPx"
        val cached = bitmapCache.get(cacheKey)
        if (cached != null) {
            onResult(cached)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = pdfEngine.renderPage(file, pageIndex, targetWidthPx)
            if (bitmap != null) {
                bitmapCache.put(cacheKey, bitmap)
            }
            withContext(Dispatchers.Main) {
                onResult(bitmap)
            }
        }
    }

    fun setCurrentPage(page: Int) {
        val count = _uiState.value.pageCount
        if (count > 0 && page in 0 until count) {
            _uiState.value = _uiState.value.copy(currentPage = page)
        }
    }

    fun setZoomLevel(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomLevel = zoom.coerceIn(0.8f, 5.0f))
    }

    fun setActiveTool(tool: ViewerTool) {
        _uiState.value = _uiState.value.copy(activeTool = tool)
    }

    fun setSelectedColor(color: Int) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(strokeWidth = width.coerceIn(1f, 20f))
    }

    fun toggleNightMode() {
        _uiState.value = _uiState.value.copy(isNightMode = !_uiState.value.isNightMode)
    }

    fun toggleContinuousScroll() {
        _uiState.value = _uiState.value.copy(isContinuousScroll = !_uiState.value.isContinuousScroll)
    }

    fun setInfoDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isInfoDialogOpen = visible)
    }

    fun openStickyNoteDialog(annotation: PdfAnnotationData) {
        _uiState.value = _uiState.value.copy(activeStickyNoteDialog = annotation)
    }

    fun closeStickyNoteDialog() {
        _uiState.value = _uiState.value.copy(activeStickyNoteDialog = null)
    }

    fun addAnnotation(annotation: PdfAnnotationData) {
        undoStack.add(_uiState.value.annotations)
        redoStack.clear()
        val updated = _uiState.value.annotations + annotation
        _uiState.value = _uiState.value.copy(
            annotations = updated,
            hasUnsavedChanges = true
        )
    }

    fun updateStickyNoteContent(id: String, newContent: String) {
        undoStack.add(_uiState.value.annotations)
        redoStack.clear()
        val updated = _uiState.value.annotations.map {
            if (it.id == id) it.copy(contents = newContent, dateModified = System.currentTimeMillis()) else it
        }
        _uiState.value = _uiState.value.copy(
            annotations = updated,
            hasUnsavedChanges = true,
            activeStickyNoteDialog = null
        )
    }

    fun deleteAnnotation(id: String) {
        undoStack.add(_uiState.value.annotations)
        redoStack.clear()
        val updated = _uiState.value.annotations.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(
            annotations = updated,
            hasUnsavedChanges = true,
            activeStickyNoteDialog = null
        )
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_uiState.value.annotations)
            _uiState.value = _uiState.value.copy(
                annotations = previous,
                hasUnsavedChanges = true
            )
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_uiState.value.annotations)
            _uiState.value = _uiState.value.copy(
                annotations = next,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * PERMANENT EMBEDDED PDF SAVING.
     * Writes all added annotations into the actual PDF binary structure.
     */
    fun saveToCurrentFile(onComplete: (Boolean) -> Unit = {}) {
        val file = _uiState.value.currentFile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val result = withContext(Dispatchers.IO) {
                // Annotations that need to be serialized into the PDF
                val toAdd = _uiState.value.annotations.filter { !it.isSavedInPdf }
                pdfEngine.saveAnnotations(
                    sourceFile = file,
                    outputFile = file,
                    annotationsToAdd = toAdd,
                    annotationIdsToDelete = emptySet()
                )
            }

            if (result.isSuccess) {
                // Invalidate bitmap cache so newly saved annotations render into the PDF
                bitmapCache.evictAll()
                val refreshedAnnots = withContext(Dispatchers.IO) {
                    pdfEngine.loadAnnotations(file)
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    hasUnsavedChanges = false,
                    annotations = refreshedAnnots,
                    statusMessage = "Annotations permanently saved to PDF"
                )
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    statusMessage = "Save failed: ${result.exceptionOrNull()?.localizedMessage}"
                )
                onComplete(false)
            }
        }
    }

    /**
     * Export an annotated copy to a user-selected SAF destination URI.
     */
    fun exportToDestinationUri(context: Context, targetUri: Uri) {
        val currentFile = _uiState.value.currentFile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            withContext(Dispatchers.IO) {
                try {
                    // First save annotations to temp file
                    val tempExport = File(context.cacheDir, "export_${System.currentTimeMillis()}.pdf")
                    val toAdd = _uiState.value.annotations.filter { !it.isSavedInPdf }

                    pdfEngine.saveAnnotations(
                        sourceFile = currentFile,
                        outputFile = tempExport,
                        annotationsToAdd = toAdd,
                        annotationIdsToDelete = emptySet()
                    )

                    // Stream to SAF destination URI
                    context.contentResolver.openOutputStream(targetUri)?.use { out ->
                        tempExport.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    tempExport.delete()

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            statusMessage = "Exported copy successfully"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Export copy failed", e)
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            statusMessage = "Export failed: ${e.localizedMessage}"
                        )
                    }
                }
            }
        }
    }

    fun rotateCurrentPage(degrees: Int) {
        val file = _uiState.value.currentFile ?: return
        val pageIdx = _uiState.value.currentPage
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                val res = pdfEngine.rotatePage(file, file, pageIdx, degrees)
                if (res.isSuccess) {
                    bitmapCache.evictAll()
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statusMessage = "Page ${pageIdx + 1} rotated by $degrees°"
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteCurrentPage() {
        val file = _uiState.value.currentFile ?: return
        val pageIdx = _uiState.value.currentPage
        if (_uiState.value.pageCount <= 1) {
            _uiState.value = _uiState.value.copy(statusMessage = "Cannot delete the only page in document")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                val res = pdfEngine.deletePages(file, file, listOf(pageIdx))
                if (res.isSuccess) {
                    bitmapCache.evictAll()
                    val newCount = pdfEngine.getPageCount(file)
                    val newCurrent = pageIdx.coerceAtMost(newCount - 1)
                    val updatedAnnots = pdfEngine.loadAnnotations(file)

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            pageCount = newCount,
                            currentPage = newCurrent,
                            annotations = updatedAnnots,
                            isLoading = false,
                            statusMessage = "Page ${pageIdx + 1} deleted"
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun searchDocument(query: String) {
        val file = _uiState.value.currentFile ?: return
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchQuery = "", searchResults = emptyList(), currentSearchIndex = -1)
            return
        }

        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                pdfEngine.searchText(file, query)
            }
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                searchResults = results,
                currentSearchIndex = if (results.isNotEmpty()) 0 else -1,
                currentPage = if (results.isNotEmpty()) results[0].pageIndex else _uiState.value.currentPage,
                statusMessage = if (results.isNotEmpty()) "Found ${results.size} matches" else "No matches found"
            )
        }
    }

    fun nextSearchResult() {
        val results = _uiState.value.searchResults
        if (results.isEmpty()) return
        val nextIdx = (_uiState.value.currentSearchIndex + 1) % results.size
        _uiState.value = _uiState.value.copy(
            currentSearchIndex = nextIdx,
            currentPage = results[nextIdx].pageIndex
        )
    }

    fun prevSearchResult() {
        val results = _uiState.value.searchResults
        if (results.isEmpty()) return
        val prevIdx = if (_uiState.value.currentSearchIndex - 1 < 0) results.size - 1 else _uiState.value.currentSearchIndex - 1
        _uiState.value = _uiState.value.copy(
            currentSearchIndex = prevIdx,
            currentPage = results[prevIdx].pageIndex
        )
    }

    fun fillFormField(name: String, value: String) {
        val file = _uiState.value.currentFile ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pdfEngine.fillFormFields(file, file, mapOf(name to value))
                bitmapCache.evictAll()
                val updatedFields = pdfEngine.getFormFields(file)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        formFields = updatedFields,
                        statusMessage = "Form field updated"
                    )
                }
            }
        }
    }

    fun updateMetadata(title: String?, author: String?, subject: String?, keywords: String?) {
        val file = _uiState.value.currentFile ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val res = pdfEngine.updateDocumentMetadata(file, file, title, author, subject, keywords)
                if (res.isSuccess) {
                    val updatedInfo = pdfEngine.getDocumentInfo(file)
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            documentInfo = updatedInfo,
                            statusMessage = "Document metadata updated"
                        )
                    }
                }
            }
        }
    }

    fun closeDocument() {
        bitmapCache.evictAll()
        undoStack.clear()
        redoStack.clear()
        _uiState.value = ViewerUiState()
    }
}
