package com.razaexplores.bookscraft.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BookscraftPrimaryDark,
    onPrimary = BookscraftOnPrimaryDark,
    primaryContainer = BookscraftPrimaryContainerDark,
    onPrimaryContainer = BookscraftOnPrimaryContainerDark,
    secondary = BookscraftSecondaryDark,
    onSecondary = BookscraftOnSecondaryDark,
    secondaryContainer = BookscraftSecondaryContainerDark,
    onSecondaryContainer = BookscraftOnSecondaryContainerDark,
    tertiary = BookscraftTertiaryDark,
    onTertiary = BookscraftOnTertiaryDark,
    tertiaryContainer = BookscraftTertiaryContainerDark,
    onTertiaryContainer = BookscraftOnTertiaryContainerDark,
    background = BookscraftBackgroundDark,
    onBackground = BookscraftOnBackgroundDark,
    surface = BookscraftSurfaceDark,
    onSurface = BookscraftOnSurfaceDark,
    surfaceVariant = BookscraftSurfaceVariantDark,
    onSurfaceVariant = BookscraftOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = BookscraftPrimaryLight,
    onPrimary = BookscraftOnPrimaryLight,
    primaryContainer = BookscraftPrimaryContainerLight,
    onPrimaryContainer = BookscraftOnPrimaryContainerLight,
    secondary = BookscraftSecondaryLight,
    onSecondary = BookscraftOnSecondaryLight,
    secondaryContainer = BookscraftSecondaryContainerLight,
    onSecondaryContainer = BookscraftOnSecondaryContainerLight,
    tertiary = BookscraftTertiaryLight,
    onTertiary = BookscraftOnTertiaryLight,
    tertiaryContainer = BookscraftTertiaryContainerLight,
    onTertiaryContainer = BookscraftOnTertiaryContainerLight,
    background = BookscraftBackgroundLight,
    onBackground = BookscraftOnBackgroundLight,
    surface = BookscraftSurfaceLight,
    onSurface = BookscraftOnSurfaceLight,
    surfaceVariant = BookscraftSurfaceVariantLight,
    onSurfaceVariant = BookscraftOnSurfaceVariantLight
)

@Composable
fun BookscraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use crafted brand colors by default for consistent Bookscraft identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
