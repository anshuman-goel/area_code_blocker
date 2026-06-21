package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BlockedLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedLogDao {
    @Query("SELECT * FROM blocked_logs ORDER BY timestamp DESC")
    fun getAllBlockedLogs(): Flow<List<BlockedLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedLog: BlockedLog)

    @Query("DELETE FROM blocked_logs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM blocked_logs")
    suspend fun clearAll()
}
