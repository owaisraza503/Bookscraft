package com.razaexplores.bookscraft.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.core.pdf.model.SearchMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSheet(
    query: String,
    results: List<SearchMatch>,
    currentIndex: Int,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSelectMatch: (SearchMatch) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        onQueryChange(it)
                        onSearch(it)
                    },
                    placeholder = { Text("Search text in PDF…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange(""); onSearch("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                if (results.isNotEmpty()) {
                    Text(
                        text = "${currentIndex + 1}/${results.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(onClick = onPrev) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                    }

                    IconButton(onClick = onNext) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close search")
                }
            }

            // Results snippets list
            if (results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(results) { match ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = match.textSnippet,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "Page ${match.pageIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable { onSelectMatch(match) }
                        )
                        HorizontalDivider()
                    }
                }
            } else if (query.isNotBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "No matches found for \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
