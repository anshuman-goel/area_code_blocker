package com.example.data.repository

import com.example.data.dao.BlockedAreaCodeDao
import com.example.data.dao.BlockedLogDao
import com.example.data.dao.BlockedKeywordDao
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedLog
import com.example.data.entity.BlockedKeyword
import kotlinx.coroutines.flow.Flow

class BlockerRepository(
    private val blockedAreaCodeDao: BlockedAreaCodeDao,
    private val blockedLogDao: BlockedLogDao,
    private val blockedKeywordDao: BlockedKeywordDao
) {
    val allBlockedAreaCodes: Flow<List<BlockedAreaCode>> = blockedAreaCodeDao.getAllBlockedAreaCodes()
    val allBlockedLogs: Flow<List<BlockedLog>> = blockedLogDao.getAllBlockedLogs()
    val allBlockedKeywords: Flow<List<BlockedKeyword>> = blockedKeywordDao.getAllBlockedKeywords()

    suspend fun getBlockedAreaCodesList(): List<BlockedAreaCode> {
        return blockedAreaCodeDao.getAllBlockedAreaCodesList()
    }

    suspend fun getBlockedKeywordsList(): List<BlockedKeyword> {
        return blockedKeywordDao.getAllBlockedKeywordsList()
    }

    suspend fun insertAreaCode(areaCode: String) {
        val cleanCode = areaCode.trim().filter { it.isDigit() }
        if (cleanCode.isNotEmpty()) {
            blockedAreaCodeDao.insert(BlockedAreaCode(areaCode = cleanCode))
        }
    }

    suspend fun deleteAreaCode(areaCode: String) {
        blockedAreaCodeDao.deleteByAreaCode(areaCode)
    }

    suspend fun insertKeyword(keyword: String) {
        val clean = keyword.trim()
        if (clean.isNotEmpty()) {
            blockedKeywordDao.insert(BlockedKeyword(keyword = clean))
        }
    }

    suspend fun deleteKeyword(keyword: String) {
        blockedKeywordDao.deleteByKeyword(keyword)
    }

    suspend fun insertLog(log: BlockedLog) {
        blockedLogDao.insert(log)
    }

    suspend fun deleteLogById(id: Int) {
        blockedLogDao.deleteById(id)
    }

    suspend fun clearLogs() {
        blockedLogDao.clearAll()
    }
}
