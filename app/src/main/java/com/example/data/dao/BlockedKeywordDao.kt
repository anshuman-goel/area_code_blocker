package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BlockedKeyword
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedKeywordDao {
    @Query("SELECT * FROM blocked_keywords ORDER BY dateAdded DESC")
    fun getAllBlockedKeywords(): Flow<List<BlockedKeyword>>

    @Query("SELECT * FROM blocked_keywords")
    suspend fun getAllBlockedKeywordsList(): List<BlockedKeyword>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedKeyword: BlockedKeyword)

    @Delete
    suspend fun delete(blockedKeyword: BlockedKeyword)

    @Query("DELETE FROM blocked_keywords WHERE keyword = :keyword")
    suspend fun deleteByKeyword(keyword: String)
}
