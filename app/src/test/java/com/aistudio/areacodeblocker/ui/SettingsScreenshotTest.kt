package com.aistudio.areacodeblocker.ui

import android.app.Application
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import com.example.R
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppVersion
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
        application.getSharedPreferences("blocker_prefs", 0).edit().clear().commit()
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
                    viewModel = viewModel,
                )
            }
        }

        // Capture the dialog specifically
        composeTestRule.onNodeWithTag("settings_dialog_surface", useUnmergedTree = true).captureRoboImage(filePath = "src/test/screenshots/settings_dialog.png")
    }

    @Test
    fun settings_dialog_protection_section() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    show = true,
                    onDismiss = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_protection").performClick()
        composeTestRule.onNodeWithText("SMS Notification Guard").assertExists()
        composeTestRule.onNodeWithTag("settings_dialog_surface", useUnmergedTree = true)
            .captureRoboImage(filePath = "src/test/screenshots/settings_dialog_protection.png")
    }

    @Test
    fun settings_dialog_legal_section() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    show = true,
                    onDismiss = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_about").performClick()
        composeTestRule.onNodeWithText("Legal & About").assertExists()
        val application = ApplicationProvider.getApplicationContext<Application>()
        val expectedVersionText = application.getString(
            R.string.app_version,
            AppVersion.name(application),
        )
        composeTestRule.onNodeWithText(expectedVersionText).assertExists()
        composeTestRule.onNodeWithText("Privacy Policy").assertExists()
        composeTestRule.onNodeWithText("Terms of Use").assertExists()
        composeTestRule.onNodeWithText("Open-Source License").assertExists()
        composeTestRule.onNodeWithText("Source Code").assertExists()
        composeTestRule.onNodeWithTag("settings_dialog_surface", useUnmergedTree = true)
            .captureRoboImage(filePath = "src/test/screenshots/settings_dialog_legal.png")
    }

    @Test
    fun settings_dialog_dark_mode_and_bottom() {
        val viewModel = createViewModel()
        
        composeTestRule.setContent {
            MyApplicationTheme(themeSetting = "Dark") {
                SettingsDialog(
                    show = true,
                    onDismiss = {},
                    viewModel = viewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_about").performClick()
        composeTestRule.onNodeWithText("Data Sources & Attributions").assertExists()

        // Capture the whole dialog surface
        composeTestRule.onNodeWithTag("settings_dialog_surface", useUnmergedTree = true).captureRoboImage(filePath = "src/test/screenshots/settings_dialog_dark_bottom.png")
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
        override fun getBlockedLogsPagingSource(): PagingSource<Int, BlockedLog> =
            object : PagingSource<Int, BlockedLog>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BlockedLog> =
                    LoadResult.Page(emptyList(), null, null)
                override fun getRefreshKey(state: PagingState<Int, BlockedLog>): Int? = null
            }
        override fun observeTotalCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeCallCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeSmsCount(): Flow<Int> = MutableStateFlow(0)
        override suspend fun insert(blockedLog: BlockedLog) {}
        override suspend fun deleteById(id: Int) {}
        override suspend fun clearAll() {}
    }
}
