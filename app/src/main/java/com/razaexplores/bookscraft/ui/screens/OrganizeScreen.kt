package com.razaexplores.bookscraft.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeScreen(
    viewModel: PdfViewerViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    var selectedPageIndex by remember { mutableStateOf(uiState.currentPage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organize Pages (${uiState.pageCount})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Rotate Clockwise
                    IconButton(onClick = { viewModel.rotateCurrentPage(90) }) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate 90° Clockwise")
                    }
                    // Rotate Counter-Clockwise
                    IconButton(onClick = { viewModel.rotateCurrentPage(-90) }) {
                        Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "Rotate 90° Counter-Clockwise")
                    }
                    // Delete page
                    IconButton(
                        onClick = { viewModel.deleteCurrentPage() },
                        enabled = uiState.pageCount > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Page",
                            tint = if (uiState.pageCount > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.rotateCurrentPage(90) }
                    ) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate Page")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.deleteCurrentPage() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = uiState.pageCount > 1
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Page")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(uiState.pageCount) { pageIndex ->
                val isSelected = pageIndex == uiState.currentPage
                var pageBitmap by remember(pageIndex, uiState.pageCount) { mutableStateOf<Bitmap?>(null) }

                LaunchedEffect(pageIndex, uiState.pageCount) {
                    viewModel.getPageBitmap(pageIndex, 280) { bmp ->
                        pageBitmap = bmp
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.setCurrentPage(pageIndex)
                            selectedPageIndex = pageIndex
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (pageBitmap != null) {
                                Image(
                                    bitmap = pageBitmap!!.asImageBitmap(),
                                    contentDescription = "Page ${pageIndex + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Page ${pageIndex + 1}",
                                style = MaterialTheme.typography.labelLarge
                            )

                            if (isSelected) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("Active", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
