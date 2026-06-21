package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_keywords")
data class BlockedKeyword(
    @PrimaryKey val keyword: String,
    val dateAdded: Long = System.currentTimeMillis()
)
