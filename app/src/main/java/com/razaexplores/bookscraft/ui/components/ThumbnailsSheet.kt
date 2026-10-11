package com.razaexplores.bookscraft.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailsSheet(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onLoadThumbnail: (pageIndex: Int, onResult: (Bitmap?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Pages Overview ($pageCount)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(bottom = 24.dp)
            ) {
                items(pageCount) { pageIndex ->
                    val isSelected = pageIndex == currentPage
                    var thumbBitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }

                    LaunchedEffect(pageIndex) {
                        onLoadThumbnail(pageIndex) { bmp ->
                            thumbBitmap = bmp
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onPageSelected(pageIndex)
                                onDismiss()
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
                        ) {
                            if (thumbBitmap != null) {
                                Image(
                                    bitmap = thumbBitmap!!.asImageBitmap(),
                                    contentDescription = "Page ${pageIndex + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Page ${pageIndex + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
