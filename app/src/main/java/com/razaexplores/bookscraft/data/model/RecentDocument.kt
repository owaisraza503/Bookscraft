package com.razaexplores.bookscraft.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val fileName: String,
    val filePath: String? = null,
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 0,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val lastReadPage: Int = 0,
    val isFavorite: Boolean = false
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "Unknown size"
            val kb = fileSizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format(java.util.Locale.US, "%.1f MB", mb)
            } else {
                String.format(java.util.Locale.US, "%.0f KB", kb)
            }
        }
}
