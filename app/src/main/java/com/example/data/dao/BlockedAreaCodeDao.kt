package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BlockedAreaCode
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAreaCodeDao {
    @Query("SELECT * FROM blocked_area_codes ORDER BY dateAdded DESC")
    fun getAllBlockedAreaCodes(): Flow<List<BlockedAreaCode>>

    @Query("SELECT * FROM blocked_area_codes")
    suspend fun getAllBlockedAreaCodesList(): List<BlockedAreaCode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedAreaCode: BlockedAreaCode)

    @Delete
    suspend fun delete(blockedAreaCode: BlockedAreaCode)

    @Query("DELETE FROM blocked_area_codes WHERE areaCode = :areaCode")
    suspend fun deleteByAreaCode(areaCode: String)
}
