package com.aistudio.areacodeblocker.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.ui.LegalAcceptanceScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LegalAcceptanceScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun legal_acceptance_unchecked() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LegalAcceptanceScreen(onAccept = {})
            }
        }

        composeTestRule.onNodeWithTag("legal_accept_button").assertIsNotEnabled()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/legal_acceptance_unchecked.png"
        )
    }

    @Test
    fun legal_acceptance_checked() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LegalAcceptanceScreen(onAccept = {})
            }
        }

        composeTestRule.onNodeWithTag("legal_acknowledgment_checkbox").performClick()
        composeTestRule.onNodeWithTag("legal_accept_button").assertIsEnabled()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/legal_acceptance_checked.png"
        )
    }

    @Test
    fun agree_invokes_acceptance_callback_only_after_acknowledgment() {
        var accepted = false
        composeTestRule.setContent {
            MyApplicationTheme {
                LegalAcceptanceScreen(onAccept = { accepted = true })
            }
        }

        composeTestRule.onNodeWithTag("legal_accept_button").performClick()
        assertTrue(!accepted)

        composeTestRule.onNodeWithTag("legal_acknowledgment_checkbox").performClick()
        composeTestRule.onNodeWithTag("legal_accept_button").performClick()
        assertTrue(accepted)
    }
}
