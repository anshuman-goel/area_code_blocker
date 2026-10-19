package com.aistudio.areacodeblocker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.ui.components.RegionalBlockCard
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RegionalBlockCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SMS area code silencing control is visible and can be disabled`() {
        setUnitedStatesCard()

        composeTestRule.onNodeWithText("Also silence SMS").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sms_area_code_silencing_switch").assertIsOn()
        composeTestRule.onNodeWithTag("sms_area_code_silencing_switch").performClick()
        composeTestRule.onNodeWithTag("sms_area_code_silencing_switch").assertIsOff()
    }

    @Test
    fun `United States menu selects Federal Government`() {
        setUnitedStatesCard()

        composeTestRule.onNodeWithText("Alabama").performClick()
        composeTestRule.onNodeWithText("Federal Government")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Includes: 710").assertIsDisplayed()
    }

    @Test
    fun `United States menu scrolls to and selects Wyoming`() {
        setUnitedStatesCard()

        composeTestRule.onNodeWithText("Alabama").performClick()
        composeTestRule.onNodeWithText("Wyoming")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Includes: 307").assertIsDisplayed()
    }

    private fun setUnitedStatesCard() {
        var selectedCountryIndex by mutableIntStateOf(0)
        var selectedStateIndex by mutableIntStateOf(0)
        var silenceSmsFromBlockedAreaCodes by androidx.compose.runtime.mutableStateOf(true)

        composeTestRule.setContent {
            MyApplicationTheme {
                RegionalBlockCard(
                    selectedCountryIndex = selectedCountryIndex,
                    selectedStateIndex = selectedStateIndex,
                    onSelectedCountryIndexChange = { selectedCountryIndex = it },
                    onSelectedStateIndexChange = { selectedStateIndex = it },
                    silenceSmsFromBlockedAreaCodes = silenceSmsFromBlockedAreaCodes,
                    onSilenceSmsFromBlockedAreaCodesChange = {
                        silenceSmsFromBlockedAreaCodes = it
                    },
                    onBlockAreaCodes = { _, _ -> },
                )
            }
        }
    }
}
