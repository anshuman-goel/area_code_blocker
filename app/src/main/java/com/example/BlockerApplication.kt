package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.BlockerRepository

class BlockerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { BlockerRepository(database.blockedAreaCodeDao(), database.blockedLogDao(), database.blockedKeywordDao()) }
}
