package com.razaexplores.bookscraft.data.local

import androidx.room.*
import com.razaexplores.bookscraft.data.model.RecentDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDocumentDao {
    @Query("SELECT * FROM recent_documents ORDER BY lastOpenedTimestamp DESC")
    fun getAllRecentDocuments(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents WHERE isFavorite = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getFavoriteDocuments(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents WHERE uriString = :uriString LIMIT 1")
    suspend fun getDocumentByUri(uriString: String): RecentDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(document: RecentDocument): Long

    @Query("UPDATE recent_documents SET lastReadPage = :page, lastOpenedTimestamp = :timestamp WHERE uriString = :uriString")
    suspend fun updateReadingProgress(uriString: String, page: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE recent_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun delete(document: RecentDocument)

    @Query("DELETE FROM recent_documents WHERE id = :id")
    suspend fun deleteById(id: Long)
}
