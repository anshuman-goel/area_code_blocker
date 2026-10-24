package com.example.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
    private val blockedKeywordDao: BlockedKeywordDao,
) {
    val allBlockedAreaCodes: Flow<List<BlockedAreaCode>> = blockedAreaCodeDao.getAllBlockedAreaCodes()
    val blockedLogs: Flow<PagingData<BlockedLog>> = Pager(
        config = PagingConfig(
            pageSize = LOG_PAGE_SIZE,
            prefetchDistance = LOG_PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = blockedLogDao::getBlockedLogsPagingSource,
    ).flow
    val totalLogCount: Flow<Int> = blockedLogDao.observeTotalCount()
    val callLogCount: Flow<Int> = blockedLogDao.observeCallCount()
    val smsLogCount: Flow<Int> = blockedLogDao.observeSmsCount()
    val allBlockedKeywords: Flow<List<BlockedKeyword>> = blockedKeywordDao.getAllBlockedKeywords()

    suspend fun getBlockedAreaCodesList(): List<BlockedAreaCode> =
        blockedAreaCodeDao.getAllBlockedAreaCodesList()

    suspend fun getBlockedKeywordsList(): List<BlockedKeyword> =
        blockedKeywordDao.getAllBlockedKeywordsList()

    suspend fun insertAreaCode(areaCode: String, regionLabel: String? = null) {
        val cleanCode = areaCode.trim().filter { it.isDigit() }
        if (cleanCode.isNotEmpty()) {
            blockedAreaCodeDao.insert(BlockedAreaCode(areaCode = cleanCode, regionLabel = regionLabel))
        }
    }

    suspend fun deleteAreaCode(areaCode: String) {
        blockedAreaCodeDao.deleteByAreaCode(areaCode)
    }

    suspend fun deleteAreaCodesByRegion(label: String) {
        blockedAreaCodeDao.deleteByRegionLabel(label)
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

    private companion object {
        const val LOG_PAGE_SIZE = 30
        const val LOG_PREFETCH_DISTANCE = 10
    }
}
