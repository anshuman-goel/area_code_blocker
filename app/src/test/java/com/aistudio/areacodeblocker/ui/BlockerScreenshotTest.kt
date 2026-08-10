package com.aistudio.areacodeblocker.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode

import androidx.test.core.app.ApplicationProvider
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.ui.BlockerHomeScreen
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

private const val TEST_TIMESTAMP = 1715000000000L // Fixed timestamp for screenshots

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class BlockerScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private fun createViewModel(
        areaCodes: List<BlockedAreaCode> = emptyList(),
        keywords: List<BlockedKeyword> = emptyList(),
        logs: List<BlockedLog> = emptyList(),
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

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_empty.png")
    }

    @Test
    fun dashboard_with_data() {
        val viewModel = createViewModel(
            areaCodes = listOf(BlockedAreaCode(areaCode = "512"), BlockedAreaCode(areaCode = "212")),
            keywords = listOf(BlockedKeyword(keyword = "lottery"), BlockedKeyword(keyword = "crypto")),
            logs = listOf(
                BlockedLog(id = 1, phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)", timestamp = TEST_TIMESTAMP),
                BlockedLog(id = 2, phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)", timestamp = TEST_TIMESTAMP)
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

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_populated.png")
    }

    @Test
    fun dashboard_with_grouped_regions() {
        val viewModel = createViewModel(
            areaCodes = listOf(
                BlockedAreaCode(areaCode = "205", regionLabel = "United States - Alabama"),
                BlockedAreaCode(areaCode = "251", regionLabel = "United States - Alabama"),
                BlockedAreaCode(areaCode = "512", regionLabel = null)
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

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_grouped.png")
    }

    @Test
    fun dashboard_logs_tab_shows_logs() {
        val viewModel = createViewModel(
            logs = listOf(
                BlockedLog(id = 3, phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)", timestamp = TEST_TIMESTAMP),
                BlockedLog(id = 4, phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)", timestamp = TEST_TIMESTAMP)
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

        // Switch to Logs tab and capture the logs UI
        composeTestRule.onNodeWithTag("logs_tab").performClick()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_logs.png")
    }

    @Test
    fun dashboard_populated_dark_mode_scrolled_middle() {
        val viewModel = createViewModel(
            areaCodes = listOf(BlockedAreaCode(areaCode = "512"), BlockedAreaCode(areaCode = "212")),
            keywords = listOf(BlockedKeyword(keyword = "lottery"), BlockedKeyword(keyword = "crypto")),
            logs = listOf(
                BlockedLog(id = 1, phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)", timestamp = TEST_TIMESTAMP),
                BlockedLog(id = 2, phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)", timestamp = TEST_TIMESTAMP)
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeSetting = "Dark") {
                BlockerHomeScreen(
                    viewModel = viewModel,
                    initContactsGranted = true,
                    initPhoneNumbersGranted = true,
                    initCallScreeningGranted = true,
                    initNotificationListenerGranted = true
                )
            }
        }

        // Scroll to the statistics grid to capture the middle section
        composeTestRule.onNodeWithTag("main_lazy_column", useUnmergedTree = true).performScrollToNode(hasTestTag("statistics_grid"))
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_populated_dark_middle.png")
    }

    @Test
    fun dashboard_populated_dark_mode_scrolled_bottom() {
        val viewModel = createViewModel(
            areaCodes = listOf(BlockedAreaCode(areaCode = "512"), BlockedAreaCode(areaCode = "212")),
            keywords = listOf(BlockedKeyword(keyword = "lottery"), BlockedKeyword(keyword = "crypto")),
            logs = listOf(
                BlockedLog(id = 1, phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)", timestamp = TEST_TIMESTAMP),
                BlockedLog(id = 2, phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)", timestamp = TEST_TIMESTAMP)
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeSetting = "Dark") {
                BlockerHomeScreen(
                    viewModel = viewModel,
                    initContactsGranted = true,
                    initPhoneNumbersGranted = true,
                    initCallScreeningGranted = true,
                    initNotificationListenerGranted = true
                )
            }
        }

        // Scroll to the bottom cards (keywords)
        composeTestRule.onNodeWithTag("main_lazy_column", useUnmergedTree = true).performScrollToNode(hasTestTag("keyword_input"))
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_populated_dark_bottom.png")
    }

    @Test
    @Config(qualifiers = "w411dp-h1000dp-xhdpi") // Custom tall device to see more content at once
    fun dashboard_populated_dark_mode_tall_view() {
        val viewModel = createViewModel(
            areaCodes = listOf(BlockedAreaCode(areaCode = "512"), BlockedAreaCode(areaCode = "212")),
            keywords = listOf(BlockedKeyword(keyword = "lottery"), BlockedKeyword(keyword = "crypto")),
            logs = listOf(
                BlockedLog(id = 1, phoneNumber = "+15125550199", areaCode = "512", messageBody = null, type = "CALL", senderName = "Unknown (Blocked Area Code)", timestamp = TEST_TIMESTAMP),
                BlockedLog(id = 2, phoneNumber = "SpamSender", areaCode = "Unknown", messageBody = "Claim your lottery prize now!", type = "SMS (Silenced)", senderName = "Blocked Content (Keyword)", timestamp = TEST_TIMESTAMP)
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeSetting = "Dark") {
                BlockerHomeScreen(
                    viewModel = viewModel,
                    initContactsGranted = true,
                    initPhoneNumbersGranted = true,
                    initCallScreeningGranted = true,
                    initNotificationListenerGranted = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/blocker_populated_dark_tall.png")
    }

    // Reuse Fake DAOs from BlockerViewModelTest logic
    class FakeAreaCodeDao(initial: List<BlockedAreaCode>) : BlockedAreaCodeDao {
        private val flow = MutableStateFlow(initial)
        override fun getAllBlockedAreaCodes(): Flow<List<BlockedAreaCode>> = flow
        override suspend fun getAllBlockedAreaCodesList(): List<BlockedAreaCode> = flow.value
        override suspend fun insert(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun delete(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun deleteByAreaCode(areaCode: String) {}
        override suspend fun deleteByRegionLabel(label: String) {}
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
