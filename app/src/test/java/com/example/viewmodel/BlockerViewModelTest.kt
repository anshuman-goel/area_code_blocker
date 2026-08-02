package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.BlockedAreaCodeDao
import com.example.data.dao.BlockedKeywordDao
import com.example.data.dao.BlockedLogDao
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedKeyword
import com.example.data.entity.BlockedLog
import com.example.data.repository.BlockerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BlockerViewModelTest {

    private lateinit var viewModel: BlockerViewModel
    private lateinit var repository: BlockerRepository
    private lateinit var fakeAreaCodeDao: FakeAreaCodeDao
    private lateinit var fakeKeywordDao: FakeKeywordDao
    private lateinit var fakeLogDao: FakeLogDao
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fakeAreaCodeDao = FakeAreaCodeDao()
        fakeKeywordDao = FakeKeywordDao()
        fakeLogDao = FakeLogDao()
        
        repository = BlockerRepository(fakeAreaCodeDao, fakeLogDao, fakeKeywordDao)
        
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = BlockerViewModel(application, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateInputPhoneNumber updates extractedAreaCode`() = runTest {
        viewModel.updateInputPhoneNumber("5125550199")
        assertEquals("512", viewModel.extractedAreaCode.value)
    }

    @Test
    fun `addAreaCode inserts into repository`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.blockedAreaCodes.collect {}
        }
        
        viewModel.addAreaCode("512")
        advanceUntilIdle()
        
        val blockedCodes = viewModel.blockedAreaCodes.value
        assertTrue("Expected 512 to be in $blockedCodes", blockedCodes.any { it.areaCode == "512" })
        
        collectJob.cancel()
    }

    @Test
    fun `addKeyword inserts into repository`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.blockedKeywords.collect {}
        }

        viewModel.addKeyword("spam")
        advanceUntilIdle()
        
        val blockedKeywords = viewModel.blockedKeywords.value
        assertTrue("Expected spam to be in $blockedKeywords", blockedKeywords.any { it.keyword == "spam" })
        
        collectJob.cancel()
    }

    // Fake Implementations
    class FakeAreaCodeDao : BlockedAreaCodeDao {
        private val list = mutableListOf<BlockedAreaCode>()
        private val flow = MutableStateFlow<List<BlockedAreaCode>>(emptyList())

        override fun getAllBlockedAreaCodes(): Flow<List<BlockedAreaCode>> = flow
        override suspend fun getAllBlockedAreaCodesList(): List<BlockedAreaCode> = list
        override suspend fun insert(blockedAreaCode: BlockedAreaCode) {
            list.add(blockedAreaCode)
            flow.value = list.toList()
        }
        override suspend fun delete(blockedAreaCode: BlockedAreaCode) {
            list.remove(blockedAreaCode)
            flow.value = list.toList()
        }
        override suspend fun deleteByAreaCode(areaCode: String) {
            list.removeAll { it.areaCode == areaCode }
            flow.value = list.toList()
        }
    }

    class FakeKeywordDao : BlockedKeywordDao {
        private val list = mutableListOf<BlockedKeyword>()
        private val flow = MutableStateFlow<List<BlockedKeyword>>(emptyList())

        override fun getAllBlockedKeywords(): Flow<List<BlockedKeyword>> = flow
        override suspend fun getAllBlockedKeywordsList(): List<BlockedKeyword> = list
        override suspend fun insert(blockedKeyword: BlockedKeyword) {
            list.add(blockedKeyword)
            flow.value = list.toList()
        }
        override suspend fun delete(blockedKeyword: BlockedKeyword) {
            list.remove(blockedKeyword)
            flow.value = list.toList()
        }
        override suspend fun deleteByKeyword(keyword: String) {
            list.removeAll { it.keyword == keyword }
            flow.value = list.toList()
        }
    }

    class FakeLogDao : BlockedLogDao {
        private val list = mutableListOf<BlockedLog>()
        private val flow = MutableStateFlow<List<BlockedLog>>(emptyList())

        override fun getAllBlockedLogs(): Flow<List<BlockedLog>> = flow
        override suspend fun insert(blockedLog: BlockedLog) {
            list.add(blockedLog)
            flow.value = list.toList()
        }
        override suspend fun deleteById(id: Int) {
            list.removeAll { it.id == id }
            flow.value = list.toList()
        }
        override suspend fun clearAll() {
            list.clear()
            flow.value = list.toList()
        }
    }
}
