package com.razaexplores.bookscraft.core.pdf.engine

import android.content.Context
import android.graphics.Bitmap
import com.razaexplores.bookscraft.core.pdf.model.*
import java.io.File

/**
 * High-level engine interface isolating PDF operations from the UI layer.
 */
interface PdfEngine {
    fun initialize(context: Context)

    fun getDocumentInfo(file: File): PdfDocumentInfo

    fun getPageCount(file: File): Int

    fun getPageMetrics(file: File, pageIndex: Int): PageMetrics?

    fun renderPage(file: File, pageIndex: Int, targetWidthPx: Int): Bitmap?

    fun extractText(file: File, pageIndex: Int): String

    fun searchText(file: File, query: String): List<SearchMatch>

    fun loadAnnotations(file: File): List<PdfAnnotationData>

    /**
     * Embeds the specified annotations into the PDF and saves to [outputFile].
     * Standard PDF annotation objects (/Highlight, /Text, /Underline, /StrikeOut, /Ink, /Square, /Circle)
     * are created or deleted accordingly.
     */
    fun saveAnnotations(
        sourceFile: File,
        outputFile: File,
        annotationsToAdd: List<PdfAnnotationData>,
        annotationIdsToDelete: Set<String>
    ): Result<Unit>

    fun rotatePage(sourceFile: File, outputFile: File, pageIndex: Int, degreesDelta: Int): Result<Unit>

    fun deletePages(sourceFile: File, outputFile: File, pageIndices: List<Int>): Result<Unit>

    fun reorderPage(sourceFile: File, outputFile: File, fromIndex: Int, toIndex: Int): Result<Unit>

    fun extractPages(sourceFile: File, outputFile: File, pageIndices: List<Int>): Result<Unit>

    fun mergeDocuments(sourceFiles: List<File>, outputFile: File): Result<Unit>

    fun getFormFields(file: File): List<FormFieldItem>

    fun fillFormFields(sourceFile: File, outputFile: File, fieldValues: Map<String, String>): Result<Unit>

    fun updateDocumentMetadata(
        sourceFile: File,
        outputFile: File,
        title: String?,
        author: String?,
        subject: String?,
        keywords: String?
    ): Result<Unit>
}
