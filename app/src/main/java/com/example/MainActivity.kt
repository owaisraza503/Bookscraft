package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.razaexplores.bookscraft.core.pdf.engine.PdfBoxEngine
import com.razaexplores.bookscraft.data.local.BookscraftDatabase
import com.razaexplores.bookscraft.ui.BookscraftApp
import com.razaexplores.bookscraft.ui.viewmodel.PdfViewerViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PdfViewerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pdfEngine = PdfBoxEngine()
        pdfEngine.initialize(applicationContext)

        val database = BookscraftDatabase.getDatabase(applicationContext)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PdfViewerViewModel(pdfEngine, database) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[PdfViewerViewModel::class.java]

        val initialUri: Uri? = intent?.takeIf {
            it.action == Intent.ACTION_VIEW || it.action == Intent.ACTION_SEND
        }?.data

        setContent {
            BookscraftApp(
                viewModel = viewModel,
                initialIntentUri = initialUri
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newUri: Uri? = intent.takeIf {
            it.action == Intent.ACTION_VIEW || it.action == Intent.ACTION_SEND
        }?.data
        if (newUri != null) {
            viewModel.openUri(this, newUri)
        }
    }
}
