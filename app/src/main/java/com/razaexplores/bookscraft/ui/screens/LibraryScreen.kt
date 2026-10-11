package com.razaexplores.bookscraft.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.razaexplores.bookscraft.data.model.RecentDocument
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: PdfViewerViewModel,
    onDocumentOpened: () -> Unit
) {
    val context = LocalContext.current
    val recentDocs by viewModel.recentDocuments.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Favorites

    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openUri(context, uri)
            onDocumentOpened()
        }
    }

    val filteredDocs = remember(recentDocs, searchQuery, selectedTab) {
        recentDocs.filter { doc ->
            val matchesTab = if (selectedTab == 1) doc.isFavorite else true
            val matchesQuery = if (searchQuery.isNotBlank()) {
                doc.fileName.contains(searchQuery, ignoreCase = true)
            } else true
            matchesTab && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Bookscraft",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Bookscraft",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.openSampleDocument(context)
                        onDocumentOpened()
                    }) {
                        Icon(imageVector = Icons.Outlined.AutoStories, contentDescription = "Sample Guide")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
                icon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                text = { Text("Open PDF") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("Free & Open Source", color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                Text("100% Offline", color = MaterialTheme.colorScheme.onTertiary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Permanent Embedded Annotations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every highlight, sticky note, and ink mark is written into the actual PDF file format. Open your notes seamlessly anywhere.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose File")
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.openSampleDocument(context)
                                    onDocumentOpened()
                                }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Try Guide")
                            }
                        }
                    }
                }
            }

            // Search Bar & Tabs
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter documents…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Recent (${recentDocs.size})") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Favorites (${recentDocs.count { it.isFavorite }})") }
                        )
                    }
                }
            }

            // Documents List or Empty State
            if (filteredDocs.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching documents" else "No recent documents yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Open PDF' or 'Try Guide' to read and annotate your first PDF document.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    RecentDocumentItem(
                        document = doc,
                        onClick = {
                            if (doc.filePath != null && File(doc.filePath).exists()) {
                                viewModel.openFile(context, File(doc.filePath), Uri.parse(doc.uriString), doc.fileName)
                                onDocumentOpened()
                            } else {
                                viewModel.openUri(context, Uri.parse(doc.uriString))
                                onDocumentOpened()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentDocumentItem(
    document: RecentDocument,
    onClick: () -> Unit
) {
    val formattedDate = remember(document.lastOpenedTimestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(document.lastOpenedTimestamp))
    }

    ListItem(
        headlineContent = {
            Text(
                text = document.fileName,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
        },
        supportingContent = {
            Text(
                text = "${document.pageCount} pages  •  ${document.formattedSize}  •  Opened $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
