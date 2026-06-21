package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BlockedAreaCodeDao
import com.example.data.dao.BlockedLogDao
import com.example.data.dao.BlockedKeywordDao
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedLog
import com.example.data.entity.BlockedKeyword

@Database(entities = [BlockedAreaCode::class, BlockedLog::class, BlockedKeyword::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAreaCodeDao(): BlockedAreaCodeDao
    abstract fun blockedLogDao(): BlockedLogDao
    abstract fun blockedKeywordDao(): BlockedKeywordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blocked_calls_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
