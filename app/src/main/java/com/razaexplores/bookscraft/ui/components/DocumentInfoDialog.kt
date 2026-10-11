package com.razaexplores.bookscraft.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.core.pdf.model.PdfDocumentInfo

@Composable
fun DocumentInfoDialog(
    info: PdfDocumentInfo,
    onUpdateMetadata: (title: String, author: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(info.title) { mutableStateOf(info.title) }
    var author by remember(info.author) { mutableStateOf(info.author ?: "") }

    val formattedSize = remember(info.fileSizeBytes) {
        val kb = info.fileSizeBytes / 1024.0
        val mb = kb / 1024.0
        if (mb >= 1.0) String.format(java.util.Locale.US, "%.2f MB", mb)
        else String.format(java.util.Locale.US, "%.0f KB", kb)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Document Properties")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                InfoRow(label = "Total Pages", value = "${info.pageCount}")
                InfoRow(label = "File Size", value = formattedSize)
                InfoRow(label = "Encrypted", value = if (info.isEncrypted) "Yes (Password Protected)" else "No")
                InfoRow(label = "Interactive Forms", value = if (info.hasAcroForm) "Detected" else "None")
                if (info.creator != null) {
                    InfoRow(label = "Application", value = info.creator)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdateMetadata(title, author)
                onDismiss()
            }) {
                Text("Save Properties")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
