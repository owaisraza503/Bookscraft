package com.razaexplores.bookscraft.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.core.pdf.model.FormFieldType
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormsScreen(
    viewModel: PdfViewerViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive PDF Forms") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.formFields.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Interactive Form Fields Found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This PDF document does not contain fillable AcroForm fields. You can still use the Annotation Toolbar to add text, sticky notes, and freehand signatures anywhere on the document.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.formFields) { field ->
                    var fieldValue by remember(field.fullyQualifiedName, field.currentValue) {
                        mutableStateOf(field.currentValue)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = field.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Badge {
                                    Text(text = field.type.name)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (field.type) {
                                FormFieldType.CHECKBOX -> {
                                    val isChecked = fieldValue.equals("Yes", ignoreCase = true) ||
                                            fieldValue.equals("true", ignoreCase = true) ||
                                            fieldValue == "1"
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val newVal = if (checked) "Yes" else "Off"
                                                fieldValue = newVal
                                                viewModel.fillFormField(field.fullyQualifiedName, newVal)
                                            }
                                        )
                                        Text(text = if (isChecked) "Checked" else "Unchecked")
                                    }
                                }
                                else -> {
                                    OutlinedTextField(
                                        value = fieldValue,
                                        onValueChange = {
                                            fieldValue = it
                                            viewModel.fillFormField(field.fullyQualifiedName, it)
                                        },
                                        label = { Text("Field Value") },
                                        singleLine = field.type != FormFieldType.OTHER,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
