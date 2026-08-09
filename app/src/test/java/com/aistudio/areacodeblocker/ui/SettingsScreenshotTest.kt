package com.aistudio.areacodeblocker.ui

import android.app.Application
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.ui.components.SettingsDialog
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SettingsScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private fun createViewModel(): BlockerViewModel {
        val areaDao = FakeAreaCodeDao()
        val keywordDao = FakeKeywordDao()
        val logDao = FakeLogDao()
        val repository = BlockerRepository(areaDao, logDao, keywordDao)
        val application = ApplicationProvider.getApplicationContext<Application>()
        return BlockerViewModel(application, repository)
    }

    @Test
    fun settings_dialog_initial_state() {
        val viewModel = createViewModel()
        
        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    show = true,
                    onDismiss = {},
                    viewModel = viewModel
                )
            }
        }

        // Use onNode(isRoot()) but specify which root if multiple exist
        // Or capture the dialog specifically
        composeTestRule.onNode(isRoot(), useUnmergedTree = true).captureRoboImage(filePath = "src/test/screenshots/settings_dialog.png")
    }

    @Test
    fun settings_dialog_dark_mode_and_bottom() {
        val viewModel = createViewModel()
        
        composeTestRule.setContent {
            MyApplicationTheme(themeSetting = "Dark") {
                SettingsDialog(
                    show = true,
                    onDismiss = {},
                    viewModel = viewModel
                )
            }
        }

        // Targeting the dialog's root specifically by finding a node within it and going up or using useUnmergedTree
        // For scrolling, we need to target the scrollable column
        composeTestRule.onNodeWithText("App Appearance").performTouchInput { swipeUp() }
        
        // Capture the whole root that contains the dialog
        composeTestRule.onNode(hasText("Settings"), useUnmergedTree = true).captureRoboImage(filePath = "src/test/screenshots/settings_dialog_dark_bottom.png")
    }

    // Fakes
    class FakeAreaCodeDao : BlockedAreaCodeDao {
        override fun getAllBlockedAreaCodes(): Flow<List<BlockedAreaCode>> = MutableStateFlow(emptyList())
        override suspend fun getAllBlockedAreaCodesList(): List<BlockedAreaCode> = emptyList()
        override suspend fun insert(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun delete(blockedAreaCode: BlockedAreaCode) {}
        override suspend fun deleteByAreaCode(areaCode: String) {}
        override suspend fun deleteByRegionLabel(label: String) {}
    }

    class FakeKeywordDao : BlockedKeywordDao {
        override fun getAllBlockedKeywords(): Flow<List<BlockedKeyword>> = MutableStateFlow(emptyList())
        override suspend fun getAllBlockedKeywordsList(): List<BlockedKeyword> = emptyList()
        override suspend fun insert(blockedKeyword: BlockedKeyword) {}
        override suspend fun delete(blockedKeyword: BlockedKeyword) {}
        override suspend fun deleteByKeyword(keyword: String) {}
    }

    class FakeLogDao : BlockedLogDao {
        override fun getAllBlockedLogs(): Flow<List<BlockedLog>> = MutableStateFlow(emptyList())
        override suspend fun insert(blockedLog: BlockedLog) {}
        override suspend fun deleteById(id: Int) {}
        override suspend fun clearAll() {}
    }
}
