package com.razaexplores.bookscraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.razaexplores.bookscraft.ui.theme.*
import com.razaexplores.bookscraft.ui.viewmodel.ViewerTool

@Composable
fun AnnotationToolbar(
    activeTool: ViewerTool,
    selectedColor: Int,
    strokeWidth: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    onToolSelected: (ViewerTool) -> Unit,
    onColorSelected: (Int) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPalette by remember { mutableStateOf(false) }

    val colors = listOf(
        MarkerYellow to 0xFFFFEB3B.toInt(),
        MarkerGreen to 0xFF81C784.toInt(),
        MarkerPink to 0xFFFF80AB.toInt(),
        MarkerCyan to 0xFF80D8FF.toInt(),
        MarkerOrange to 0xFFFFB74D.toInt(),
        MarkerPurple to 0xFFBA68C8.toInt()
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .padding(12.dp)
            .testTag("annotation_toolbar")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Color palette selector expandable row
            AnimatedVisibility(visible = showColorPalette) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    colors.forEach { (colorObj, intVal) ->
                        val isSelected = selectedColor == intVal
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colorObj)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onColorSelected(intVal)
                                    showColorPalette = false
                                }
                        )
                    }
                }
            }

            // Main Tools Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Hand / Pan Tool
                ToolIconButton(
                    icon = Icons.Default.PanTool,
                    contentDescription = "Pan and View",
                    isSelected = activeTool == ViewerTool.VIEW_PAN,
                    onClick = { onToolSelected(ViewerTool.VIEW_PAN) }
                )

                // Highlight Tool
                ToolIconButton(
                    icon = Icons.Default.Highlight,
                    contentDescription = "Highlight",
                    isSelected = activeTool == ViewerTool.HIGHLIGHT,
                    onClick = { onToolSelected(ViewerTool.HIGHLIGHT) }
                )

                // Underline Tool
                ToolIconButton(
                    icon = Icons.Default.FormatUnderlined,
                    contentDescription = "Underline",
                    isSelected = activeTool == ViewerTool.UNDERLINE,
                    onClick = { onToolSelected(ViewerTool.UNDERLINE) }
                )

                // Strikethrough Tool
                ToolIconButton(
                    icon = Icons.Default.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    isSelected = activeTool == ViewerTool.STRIKEOUT,
                    onClick = { onToolSelected(ViewerTool.STRIKEOUT) }
                )

                // Sticky Note Tool
                ToolIconButton(
                    icon = Icons.Default.Comment,
                    contentDescription = "Sticky Note",
                    isSelected = activeTool == ViewerTool.STICKY_NOTE,
                    onClick = { onToolSelected(ViewerTool.STICKY_NOTE) }
                )

                // Freehand Pen Tool
                ToolIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Freehand Pen",
                    isSelected = activeTool == ViewerTool.PEN,
                    onClick = { onToolSelected(ViewerTool.PEN) }
                )

                // Rectangle Shape Tool
                ToolIconButton(
                    icon = Icons.Outlined.CropSquare,
                    contentDescription = "Rectangle",
                    isSelected = activeTool == ViewerTool.RECTANGLE,
                    onClick = { onToolSelected(ViewerTool.RECTANGLE) }
                )

                // Circle Shape Tool
                ToolIconButton(
                    icon = Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Circle",
                    isSelected = activeTool == ViewerTool.CIRCLE,
                    onClick = { onToolSelected(ViewerTool.CIRCLE) }
                )

                // Eraser Tool
                ToolIconButton(
                    icon = Icons.Default.AutoFixNormal,
                    contentDescription = "Eraser",
                    isSelected = activeTool == ViewerTool.ERASER,
                    onClick = { onToolSelected(ViewerTool.ERASER) }
                )

                VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 4.dp))

                // Color trigger button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(selectedColor))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { showColorPalette = !showColorPalette }
                )

                VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 4.dp))

                // Undo
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Redo
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledIconToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick() },
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}
