package com.razaexplores.bookscraft.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.razaexplores.bookscraft.ui.screens.FormsScreen
import com.razaexplores.bookscraft.ui.screens.LibraryScreen
import com.razaexplores.bookscraft.ui.screens.OrganizeScreen
import com.razaexplores.bookscraft.ui.screens.ViewerScreen
import com.razaexplores.bookscraft.ui.theme.BookscraftTheme
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel

enum class AppScreen {
    LIBRARY,
    VIEWER,
    ORGANIZE,
    FORMS
}

@Composable
fun BookscraftApp(
    viewModel: PdfViewerViewModel,
    initialIntentUri: Uri? = null
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.LIBRARY) }

    // If app launched with an ACTION_VIEW intent containing a PDF Uri
    LaunchedEffect(initialIntentUri) {
        if (initialIntentUri != null) {
            viewModel.openUri(context, initialIntentUri)
            currentScreen = AppScreen.VIEWER
        }
    }

    BookscraftTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onDocumentOpened = { currentScreen = AppScreen.VIEWER }
                    )
                }

                AppScreen.VIEWER -> {
                    ViewerScreen(
                        viewModel = viewModel,
                        onBack = {
                            viewModel.closeDocument()
                            currentScreen = AppScreen.LIBRARY
                        },
                        onNavigateOrganize = { currentScreen = AppScreen.ORGANIZE },
                        onNavigateForms = { currentScreen = AppScreen.FORMS }
                    )
                }

                AppScreen.ORGANIZE -> {
                    OrganizeScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.VIEWER }
                    )
                }

                AppScreen.FORMS -> {
                    FormsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = AppScreen.VIEWER }
                    )
                }
            }
        }
    }
}
