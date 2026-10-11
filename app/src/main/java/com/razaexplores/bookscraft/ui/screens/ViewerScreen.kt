package com.razaexplores.bookscraft.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.razaexplores.bookscraft.ui.components.*
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    viewModel: PdfViewerViewModel,
    onBack: () -> Unit,
    onNavigateOrganize: () -> Unit,
    onNavigateForms: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isThumbnailsSheetOpen by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    // Export PDF file picker launcher
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportToDestinationUri(context, uri)
        }
    }

    // Show status messages in Snackbar
    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Synchronize page index when scrolling
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (uiState.pageCount > 0) {
            viewModel.setCurrentPage(listState.firstVisibleItemIndex)
        }
    }

    // Scroll to page when changed via search or thumbnails
    LaunchedEffect(uiState.currentPage) {
        if (listState.firstVisibleItemIndex != uiState.currentPage && uiState.currentPage in 0 until uiState.pageCount) {
            listState.animateScrollToItem(uiState.currentPage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.documentInfo?.title ?: uiState.currentFile?.name ?: "Bookscraft Reader",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "Page ${uiState.currentPage + 1} of ${uiState.pageCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Document")
                    }
                },
                actions = {
                    // Save Status Badge / Action
                    if (uiState.hasUnsavedChanges) {
                        FilledTonalButton(
                            onClick = { viewModel.saveToCurrentFile() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        IconButton(onClick = { viewModel.saveToCurrentFile() }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Saved",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Search Trigger
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Text")
                    }

                    // Thumbnails Grid Trigger
                    IconButton(onClick = { isThumbnailsSheetOpen = true }) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = "Pages")
                    }

                    // Overflow Menu
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Copy") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val defaultName = "Annotated_${uiState.currentFile?.name ?: "doc.pdf"}"
                                exportPdfLauncher.launch(defaultName)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Share PDF") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val file = uiState.currentFile
                                if (file != null && file.exists()) {
                                    val shareUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                                }
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Organize Pages") },
                            leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateOrganize()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Interactive Forms") },
                            leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateForms()
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(if (uiState.isNightMode) "Normal Mode" else "Night / Inverted Mode") },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.toggleNightMode()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Document Properties") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.setInfoDialogVisible(true)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // PDF Pages List
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 96.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.pageCount) { pageIndex ->
                    var pageBitmap by remember(pageIndex, uiState.pageCount) { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(pageIndex, uiState.pageCount) {
                        viewModel.getPageBitmap(pageIndex, 1080) { bmp ->
                            pageBitmap = bmp
                        }
                    }

                    val pageAnnotations = remember(uiState.annotations, pageIndex) {
                        uiState.annotations.filter { it.pageIndex == pageIndex }
                    }

                    PdfPageView(
                        pageIndex = pageIndex,
                        bitmap = pageBitmap,
                        annotations = pageAnnotations,
                        activeTool = uiState.activeTool,
                        selectedColor = uiState.selectedColor,
                        strokeWidth = uiState.strokeWidth,
                        isNightMode = uiState.isNightMode,
                        onAddAnnotation = { viewModel.addAnnotation(it) },
                        onDeleteAnnotation = { viewModel.deleteAnnotation(it) },
                        onStickyNoteClick = { viewModel.openStickyNoteDialog(it) }
                    )
                }
            }

            // Floating Annotation Toolbar Dock
            AnnotationToolbar(
                activeTool = uiState.activeTool,
                selectedColor = uiState.selectedColor,
                strokeWidth = uiState.strokeWidth,
                canUndo = true,
                canRedo = true,
                onToolSelected = { viewModel.setActiveTool(it) },
                onColorSelected = { viewModel.setSelectedColor(it) },
                onStrokeWidthChange = { viewModel.setStrokeWidth(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Search Bar Sheet Overlay
            if (isSearchActive) {
                SearchBarSheet(
                    query = uiState.searchQuery,
                    results = uiState.searchResults,
                    currentIndex = uiState.currentSearchIndex,
                    onQueryChange = { q -> viewModel.searchDocument(q) },
                    onSearch = { q -> viewModel.searchDocument(q) },
                    onNext = { viewModel.nextSearchResult() },
                    onPrev = { viewModel.prevSearchResult() },
                    onSelectMatch = { match -> viewModel.setCurrentPage(match.pageIndex) },
                    onClose = {
                        isSearchActive = false
                        viewModel.searchDocument("")
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }

        // Thumbnails Bottom Sheet
        if (isThumbnailsSheetOpen) {
            ThumbnailsSheet(
                pageCount = uiState.pageCount,
                currentPage = uiState.currentPage,
                onPageSelected = { page -> viewModel.setCurrentPage(page) },
                onLoadThumbnail = { idx, callback ->
                    viewModel.getPageBitmap(idx, 200, callback)
                },
                onDismiss = { isThumbnailsSheetOpen = false }
            )
        }

        // Sticky Note Content Dialog
        if (uiState.activeStickyNoteDialog != null) {
            StickyNoteDialog(
                annotation = uiState.activeStickyNoteDialog!!,
                onSave = { newContent ->
                    viewModel.updateStickyNoteContent(uiState.activeStickyNoteDialog!!.id, newContent)
                },
                onDelete = {
                    viewModel.deleteAnnotation(uiState.activeStickyNoteDialog!!.id)
                },
                onDismiss = { viewModel.closeStickyNoteDialog() }
            )
        }

        // Document Properties Dialog
        if (uiState.isInfoDialogOpen && uiState.documentInfo != null) {
            DocumentInfoDialog(
                info = uiState.documentInfo!!,
                onUpdateMetadata = { title, author ->
                    viewModel.updateMetadata(title, author, null, null)
                },
                onDismiss = { viewModel.setInfoDialogVisible(false) }
            )
        }
    }
}
