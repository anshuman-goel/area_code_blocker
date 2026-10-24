package com.aistudio.areacodeblocker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.data.entity.BlockedAreaCode
import com.example.data.entity.BlockedKeyword
import com.example.ui.components.ProtectionStatusBanner
import com.example.ui.components.activeRuleCount
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ProtectionStatusBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeRuleCount_groupsRegionsAndCountsManualCodesAndKeywords() {
        val areaCodes = listOf(
            BlockedAreaCode(areaCode = "205", regionLabel = "United States - Alabama"),
            BlockedAreaCode(areaCode = "251", regionLabel = "United States - Alabama"),
            BlockedAreaCode(areaCode = "512"),
            BlockedAreaCode(areaCode = "212"),
        )
        val keywords = listOf(BlockedKeyword("lottery"), BlockedKeyword("crypto"))

        assertEquals(5, activeRuleCount(areaCodes, keywords))
    }

    @Test
    fun activeRuleCount_deduplicatesRepeatedStoredRules() {
        val areaCodes = listOf(
            BlockedAreaCode(areaCode = "205", regionLabel = "Alabama"),
            BlockedAreaCode(areaCode = "251", regionLabel = "Alabama"),
            BlockedAreaCode(areaCode = "512"),
            BlockedAreaCode(areaCode = "512"),
        )
        val keywords = listOf(BlockedKeyword("lottery"), BlockedKeyword("lottery"))

        assertEquals(3, activeRuleCount(areaCodes, keywords))
    }

    @Test
    fun keywordOnlyRules_showTotalAndChipAndInvokeRemoval() {
        var removedKeyword: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ProtectionStatusBanner(
                    isContactsGranted = true,
                    isRoleGranted = true,
                    blockedAreaCodes = emptyList(),
                    blockedKeywords = listOf(BlockedKeyword("lottery")),
                    onRemoveAreaCode = {},
                    onRemoveRegion = {},
                    onRemoveKeyword = { removedKeyword = it },
                    onRequestPermissions = {},
                    onRequestRole = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1 Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("lottery").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete keyword lottery").performClick()

        composeTestRule.runOnIdle { assertEquals("lottery", removedKeyword) }
    }

    @Test
    fun mixedRules_showCombinedTotalAndKeywordChip() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProtectionStatusBanner(
                    isContactsGranted = true,
                    isRoleGranted = true,
                    blockedAreaCodes = listOf(
                        BlockedAreaCode("205", regionLabel = "Alabama"),
                        BlockedAreaCode("251", regionLabel = "Alabama"),
                        BlockedAreaCode("512"),
                    ),
                    blockedKeywords = listOf(BlockedKeyword("lottery"), BlockedKeyword("crypto")),
                    onRemoveAreaCode = {},
                    onRemoveRegion = {},
                    onRemoveKeyword = {},
                    onRequestPermissions = {},
                    onRequestRole = {},
                )
            }
        }

        composeTestRule.onNodeWithText("4 Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("lottery").assertIsDisplayed()
        composeTestRule.onNodeWithText("crypto").assertIsDisplayed()
    }
}
