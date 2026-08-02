package com.aistudio.areacodeblocker.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.BlockerHomeScreen
import com.example.data.dao.BlockedAreaCodeDao
import com.example.data.dao.BlockedKeywordDao
import com.example.data.dao.BlockedLogDao
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedKeyword
import com.example.data.entity.BlockedLog
import com.example.data.repository.BlockerRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BlockerViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class DashboardScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private fun createViewModel(
        areaCodes: List<BlockedAreaCode> = emptyList(),
        keywords: List<BlockedKeyword> = emptyList(),
        logs: List<BlockedLog> = emptyList()
    ): BlockerViewModel {
        val areaDao = FakeAreaCodeDao(areaCodes)
        val keywordDao = FakeKeywordDao(keywords)
        val logDao = FakeLogDao(logs)
        val repository = BlockerRepository(areaDao, logDao, keywordDao)
        val application = ApplicationProvider.getApplicationContext<Application>()
        return BlockerViewModel(application, repository)
    }

    @Test
    fun dashboard_empty_state() {
        val viewModel = createViewModel()
        
        composeTestRule.setContent {
            MyApplicationTheme {
                BlockerHomeScreen(
                    viewModel = viewModel,
                    initContactsGranted = true,
                    initPhoneNumbersGranted = true,
                    initCallScreeningGranted = true,
                    initNotificationListenerGranted = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard_empty.png")
    }

    @Test
    fun dashboard_with_data() {
        val viewModel = createViewModel(
            areaCodes = listOf(BlockedAreaCode(areaCode = "512"), BlockedAreaCode(areaCode = "212")),
            keywords = listOf(BlockedKeyword(keyword = "lottery"), BlockedKeyword(keyword = "crypto")),
            logs = listOf(
                BlockedLog(phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)"),
                BlockedLog(phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)")
            )
        )
        
        composeTestRule.setContent {
            MyApplicationTheme {
                BlockerHomeScreen(
                    viewModel = viewModel,
                    initContactsGranted = true,
                    initPhoneNumbersGranted = true,
                    initCallScreeningGranted = true,
                    initNotificationListenerGranted = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard_populated.png")
    }

    // Reuse Fake DAOs from BlockerViewModelTest logic
    class FakeAreaCodeDao(initial: List<BlockedAreaCode>) : BlockedAreaCodeDao {
        private val flow = MutableStateFlow(initial)
        override fun getAllBlockedAreaCodes(): Flow<List<BlockedAreaCode>> = flow
        override suspend fun getAllBlockedAreaCodesList(): List<BlockedAreaCode> = flow.value
        override suspend fun insert(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun delete(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun deleteByAreaCode(areaCode: String) {}
    }

    class FakeKeywordDao(initial: List<BlockedKeyword>) : BlockedKeywordDao {
        private val flow = MutableStateFlow(initial)
        override fun getAllBlockedKeywords(): Flow<List<BlockedKeyword>> = flow
        override suspend fun getAllBlockedKeywordsList(): List<BlockedKeyword> = flow.value
        override suspend fun insert(blockedKeyword: BlockedKeyword) {}
        override suspend fun delete(blockedKeyword: BlockedKeyword) {}
        override suspend fun deleteByKeyword(keyword: String) {}
    }

    class FakeLogDao(initial: List<BlockedLog>) : BlockedLogDao {
        private val flow = MutableStateFlow(initial)
        override fun getAllBlockedLogs(): Flow<List<BlockedLog>> = flow
        override suspend fun insert(blockedLog: BlockedLog) {}
        override suspend fun deleteById(id: Int) {}
        override suspend fun clearAll() {}
    }
}
