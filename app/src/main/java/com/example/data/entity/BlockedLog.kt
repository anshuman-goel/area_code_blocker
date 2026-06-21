package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_logs")
data class BlockedLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val areaCode: String,
    val messageBody: String?, // Null for calls
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "CALL" or "SMS"
    val senderName: String? // Name if found, or custom details
)
