package com.example

import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationText
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Mandatory PDF Annotation Round-Trip Verification Test.
 * Verifies that Highlights, Sticky Notes, Underlines, Ink, and Shape annotations
 * are permanently serialized into standard PDF objects and successfully deserialized.
 */
class ExampleUnitTest {

  @Test
  fun testPdfAnnotationRoundTrip() {
    val document = PDDocument()
    val page = PDPage(PDRectangle.LETTER)
    document.addPage(page)

    // 1. Text Highlight annotation
    val highlight = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT)
    highlight.rectangle = PDRectangle(50f, 700f, 200f, 20f)
    highlight.color = PDColor(floatArrayOf(1.0f, 0.9f, 0.0f), PDDeviceRGB.INSTANCE) // Yellow
    highlight.contents = "Key highlighted point"
    highlight.quadPoints = floatArrayOf(
      50f, 720f,
      250f, 720f,
      50f, 700f,
      250f, 700f
    )
    page.annotations.add(highlight)

    // 2. Sticky Note comment annotation
    val stickyNote = PDAnnotationText()
    stickyNote.rectangle = PDRectangle(300f, 700f, 24f, 24f)
    stickyNote.contents = "This is a permanently embedded sticky note comment in Bookscraft."
    stickyNote.color = PDColor(floatArrayOf(1.0f, 0.8f, 0.2f), PDDeviceRGB.INSTANCE)
    page.annotations.add(stickyNote)

    // 3. Text Underline annotation
    val underline = PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE)
    underline.rectangle = PDRectangle(50f, 650f, 150f, 15f)
    underline.color = PDColor(floatArrayOf(0.1f, 0.5f, 0.9f), PDDeviceRGB.INSTANCE) // Blue
    underline.quadPoints = floatArrayOf(
      50f, 665f,
      200f, 665f,
      50f, 650f,
      200f, 650f
    )
    page.annotations.add(underline)

    // 4. Freehand Ink annotation
    val ink = PDAnnotationMarkup()
    ink.cosObject.setName(COSName.SUBTYPE, PDAnnotationMarkup.SUB_TYPE_INK)
    ink.rectangle = PDRectangle(50f, 500f, 100f, 100f)
    ink.color = PDColor(floatArrayOf(0.9f, 0.1f, 0.1f), PDDeviceRGB.INSTANCE) // Red
    val path = floatArrayOf(50f, 500f, 80f, 550f, 120f, 530f, 150f, 600f)
    ink.inkList = arrayOf(path)
    page.annotations.add(ink)

    // 5. Rectangle Shape annotation
    val square = PDAnnotationSquareCircle(PDAnnotationSquareCircle.SUB_TYPE_SQUARE)
    square.rectangle = PDRectangle(200f, 400f, 120f, 80f)
    square.color = PDColor(floatArrayOf(0.2f, 0.7f, 0.3f), PDDeviceRGB.INSTANCE)
    page.annotations.add(square)

    // Save to real PDF byte stream
    val outputStream = ByteArrayOutputStream()
    document.save(outputStream)
    document.close()

    val savedPdfBytes = outputStream.toByteArray()
    assertTrue("Saved PDF bytes must not be empty", savedPdfBytes.isNotEmpty())

    // REOPEN AND VERIFY EMBEDDED OBJECTS
    val loadedDoc = PDDocument.load(ByteArrayInputStream(savedPdfBytes))
    assertEquals(1, loadedDoc.numberOfPages)

    val loadedPage = loadedDoc.getPage(0)
    val annotations: List<PDAnnotation> = loadedPage.annotations
    assertEquals("Must contain 5 embedded PDF annotations", 5, annotations.size)

    val loadedHighlight = annotations.filterIsInstance<PDAnnotationTextMarkup>()
      .firstOrNull { it.subtype == PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT }
    assertNotNull("Highlight must be present in saved PDF", loadedHighlight)
    assertEquals("Key highlighted point", loadedHighlight?.contents)

    val loadedNote = annotations.filterIsInstance<PDAnnotationText>().firstOrNull()
    assertNotNull("Sticky note must be present in saved PDF", loadedNote)
    assertEquals("This is a permanently embedded sticky note comment in Bookscraft.", loadedNote?.contents)

    val loadedUnderline = annotations.filterIsInstance<PDAnnotationTextMarkup>()
      .firstOrNull { it.subtype == PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE }
    assertNotNull("Underline must be present in saved PDF", loadedUnderline)

    val loadedInk = annotations.filterIsInstance<PDAnnotationMarkup>()
      .firstOrNull { it.subtype == PDAnnotationMarkup.SUB_TYPE_INK }
    assertNotNull("Ink annotation must be present in saved PDF", loadedInk)
    assertNotNull(loadedInk?.inkList)

    val loadedSquare = annotations.filterIsInstance<PDAnnotationSquareCircle>()
      .firstOrNull { it.subtype == PDAnnotationSquareCircle.SUB_TYPE_SQUARE }
    assertNotNull("Square annotation must be present in saved PDF", loadedSquare)

    loadedDoc.close()
  }
}
