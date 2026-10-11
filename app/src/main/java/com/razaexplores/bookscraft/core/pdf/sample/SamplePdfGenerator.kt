package com.razaexplores.bookscraft.core.pdf.sample

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationText
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup
import java.io.File
import java.util.Calendar

object SamplePdfGenerator {
    private const val TAG = "SamplePdfGenerator"

    fun getOrCreateSamplePdf(context: Context): File {
        val sampleDir = File(context.filesDir, "samples")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val sampleFile = File(sampleDir, "Bookscraft_Welcome_Guide.pdf")
        if (sampleFile.exists() && sampleFile.length() > 500) {
            return sampleFile
        }

        try {
            val doc = PDDocument()

            // Metadata
            val info = PDDocumentInformation()
            info.title = "Welcome to Bookscraft - Architecture & Guide"
            info.author = "Bookscraft Open Source Team"
            info.subject = "Comprehensive Mobile PDF Reader & Annotator"
            info.creator = "Bookscraft Android Engine"
            info.creationDate = Calendar.getInstance()
            doc.documentInformation = info

            // Page 1: Overview & Reading Features
            val page1 = PDPage(PDRectangle.LETTER)
            doc.addPage(page1)

            PDPageContentStream(doc, page1).use { cs ->
                // Header Banner
                cs.setNonStrokingColor(13, 35, 58) // Deep Sapphire
                cs.addRect(0f, 720f, 612f, 72f)
                cs.fill()

                // Header Title
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22f)
                cs.setNonStrokingColor(255, 255, 255)
                cs.newLineAtOffset(40f, 750f)
                cs.showText("BOOKSCRAFT")
                cs.endText()

                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA, 12f)
                cs.setNonStrokingColor(220, 200, 140)
                cs.newLineAtOffset(210f, 752f)
                cs.showText("Open-Source PDF Suite for Android")
                cs.endText()

                // Section 1
                var y = 670f
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16f)
                cs.setNonStrokingColor(20, 30, 45)
                cs.newLineAtOffset(40f, y)
                cs.showText("1. Welcome to Bookscraft")
                cs.endText()

                y -= 25f
                val body1 = listOf(
                    "Bookscraft is a full-featured, completely free, and open-source PDF reader,",
                    "annotator, editor, and document organizer built natively for Android.",
                    "Enjoy zero ads, no subscriptions, no accounts, and complete offline privacy.",
                    "All your documents stay strictly on your device."
                )
                for (line in body1) {
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, 11f)
                    cs.setNonStrokingColor(60, 64, 70)
                    cs.newLineAtOffset(40f, y)
                    cs.showText(line)
                    cs.endText()
                    y -= 16f
                }

                // Section 2
                y -= 15f
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16f)
                cs.setNonStrokingColor(20, 30, 45)
                cs.newLineAtOffset(40f, y)
                cs.showText("2. Permanent Embedded PDF Annotations")
                cs.endText()

                y -= 25f
                val body2 = listOf(
                    "Unlike ordinary readers that store notes in proprietary app databases,",
                    "Bookscraft embeds every highlight, sticky note, freehand stroke, and shape",
                    "directly into the PDF file as standard ISO 32000-1 annotation objects.",
                    "When you save and share your PDF, your annotations open seamlessly in Adobe",
                    "Acrobat, Google Chrome, Apple Preview, and all standard PDF viewers."
                )
                for (line in body2) {
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, 11f)
                    cs.setNonStrokingColor(60, 64, 70)
                    cs.newLineAtOffset(40f, y)
                    cs.showText(line)
                    cs.endText()
                    y -= 16f
                }

                // Section 3
                y -= 15f
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16f)
                cs.setNonStrokingColor(20, 30, 45)
                cs.newLineAtOffset(40f, y)
                cs.showText("3. Core Reading Tools")
                cs.endText()

                y -= 25f
                val body3 = listOf(
                    "- Smooth Pinch-to-Zoom: Zoom up to 5x with razor-sharp vector rendering.",
                    "- Dual View Modes: Seamless continuous vertical scroll or single-page swipe.",
                    "- Dark & Inverted Reading: Comfortable night mode to reduce eye strain.",
                    "- Full-Document Search: Instant keyword searching with snippet previews.",
                    "- Page Thumbnails & Quick Jump: Rapid visual navigation across long documents."
                )
                for (line in body3) {
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, 11f)
                    cs.setNonStrokingColor(60, 64, 70)
                    cs.newLineAtOffset(40f, y)
                    cs.showText(line)
                    cs.endText()
                    y -= 16f
                }

                // Footer
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9f)
                cs.setNonStrokingColor(120, 130, 140)
                cs.newLineAtOffset(40f, 40f)
                cs.showText("Page 1 of 2  •  Bookscraft Android Open Source Project")
                cs.endText()
            }

            // Page 2: Hands-on Interactive Testing
            val page2 = PDPage(PDRectangle.LETTER)
            doc.addPage(page2)

            PDPageContentStream(doc, page2).use { cs ->
                // Header
                cs.setNonStrokingColor(13, 35, 58)
                cs.addRect(0f, 720f, 612f, 72f)
                cs.fill()

                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20f)
                cs.setNonStrokingColor(255, 255, 255)
                cs.newLineAtOffset(40f, 750f)
                cs.showText("Interactive Annotation Test Page")
                cs.endText()

                var y = 670f
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 15f)
                cs.setNonStrokingColor(20, 30, 45)
                cs.newLineAtOffset(40f, y)
                cs.showText("Try Out the Annotation Tools Below:")
                cs.endText()

                y -= 25f
                val instructions = listOf(
                    "1. Tap the Highlight tool in the floating toolbar to mark important passages.",
                    "2. Tap the Sticky Note tool, then tap anywhere to add a portable comment.",
                    "3. Switch to Freehand Pen mode to write, sketch diagrams, or circle text.",
                    "4. Use Page Organization in the top menu to rotate or reorder pages.",
                    "5. Hit Save or Export Copy to write permanently to the PDF file!"
                )
                for (line in instructions) {
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, 11f)
                    cs.setNonStrokingColor(50, 55, 65)
                    cs.newLineAtOffset(40f, y)
                    cs.showText(line)
                    cs.endText()
                    y -= 18f
                }

                // Sample text box for highlighting
                y -= 15f
                cs.setNonStrokingColor(245, 247, 250)
                cs.addRect(40f, y - 75f, 532f, 85f)
                cs.fill()

                cs.setStrokingColor(200, 210, 225)
                cs.setLineWidth(1f)
                cs.addRect(40f, y - 75f, 532f, 85f)
                cs.stroke()

                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12f)
                cs.setNonStrokingColor(13, 35, 58)
                cs.newLineAtOffset(55f, y - 10f)
                cs.showText("Key Architecture Principle:")
                cs.endText()

                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA, 11f)
                cs.setNonStrokingColor(40, 45, 55)
                cs.newLineAtOffset(55f, y - 30f)
                cs.showText("PDF annotations are standard dictionary objects saved directly into the file stream.")
                cs.newLineAtOffset(0f, -16f)
                cs.showText("They survive reopening, device reboots, and cross-platform exports intact.")
                cs.endText()

                // Footer
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9f)
                cs.setNonStrokingColor(120, 130, 140)
                cs.newLineAtOffset(40f, 40f)
                cs.showText("Page 2 of 2  •  Bookscraft Android Open Source Project")
                cs.endText()
            }

            // Add initial sample highlight to Page 1
            val sampleHighlight = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT)
            sampleHighlight.rectangle = PDRectangle(40f, 490f, 450f, 16f)
            sampleHighlight.color = PDColor(floatArrayOf(1.0f, 0.9f, 0.0f), PDDeviceRGB.INSTANCE)
            sampleHighlight.contents = "Permanent Embedded PDF Annotations"
            sampleHighlight.quadPoints = floatArrayOf(
                40f, 506f,
                490f, 506f,
                40f, 490f,
                490f, 490f
            )
            page1.annotations.add(sampleHighlight)

            // Add initial sticky note to Page 1
            val sampleNote = PDAnnotationText()
            sampleNote.rectangle = PDRectangle(530f, 490f, 24f, 24f)
            sampleNote.contents = "Welcome! This sticky note is embedded into the actual PDF."
            sampleNote.titlePopup = "Bookscraft Team"
            sampleNote.color = PDColor(floatArrayOf(1.0f, 0.75f, 0.0f), PDDeviceRGB.INSTANCE)
            page1.annotations.add(sampleNote)

            doc.save(sampleFile)
            doc.close()
            Log.d(TAG, "Sample PDF created successfully at: ${sampleFile.absolutePath}")
            return sampleFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create sample PDF", e)
            return sampleFile
        }
    }
}
