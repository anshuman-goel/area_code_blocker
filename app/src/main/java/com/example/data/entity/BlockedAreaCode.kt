package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_area_codes")
data class BlockedAreaCode(
    @PrimaryKey val areaCode: String,
    val dateAdded: Long = System.currentTimeMillis()
)
