package com.aistudio.areacodeblocker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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

        composeTestRule.setContent {
            MyApplicationTheme {
                RegionalBlockCard(
                    selectedCountryIndex = selectedCountryIndex,
                    selectedStateIndex = selectedStateIndex,
                    onSelectedCountryIndexChange = { selectedCountryIndex = it },
                    onSelectedStateIndexChange = { selectedStateIndex = it },
                    onBlockAreaCodes = { _, _ -> },
                )
            }
        }
    }
}
