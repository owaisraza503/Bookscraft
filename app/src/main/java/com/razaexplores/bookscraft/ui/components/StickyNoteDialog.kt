package com.razaexplores.bookscraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.core.pdf.model.PdfAnnotationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StickyNoteDialog(
    annotation: PdfAnnotationData,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var contentText by remember(annotation.id) { mutableStateOf(annotation.contents ?: "") }

    val formattedDate = remember(annotation.dateModified) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(annotation.dateModified))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(annotation.color), shape = RoundedCornerShape(4.dp))
                )
                Text(
                    text = annotation.author ?: "Sticky Note",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Page ${annotation.pageIndex + 1}  •  $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("Note comment") },
                    placeholder = { Text("Enter your comment or thoughts here…") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "This note is permanently embedded into the PDF document.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(contentText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
