package com.razaexplores.bookscraft.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.razaexplores.bookscraft.data.model.RecentDocument

@Database(entities = [RecentDocument::class], version = 1, exportSchema = false)
abstract class BookscraftDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao

    companion object {
        @Volatile
        private var INSTANCE: BookscraftDatabase? = null

        fun getDatabase(context: Context): BookscraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookscraftDatabase::class.java,
                    "bookscraft_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
