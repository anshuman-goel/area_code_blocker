package com.example.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BlockedLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedLogDao {
    @Query("SELECT * FROM blocked_logs ORDER BY timestamp DESC, id DESC")
    fun getBlockedLogsPagingSource(): PagingSource<Int, BlockedLog>

    @Query("SELECT COUNT(*) FROM blocked_logs")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_logs WHERE type = 'CALL'")
    fun observeCallCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_logs WHERE type LIKE 'SMS%'")
    fun observeSmsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedLog: BlockedLog)

    @Query("DELETE FROM blocked_logs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM blocked_logs")
    suspend fun clearAll()
}
