package com.aistudio.areacodeblocker.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.ui.AppOnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(BlockerTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class OnboardingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun onboarding_none_granted() {
    composeTestRule.setContent { 
      MyApplicationTheme { 
        AppOnboardingScreen(
          isContactsGranted = false,
          isPhoneNumbersGranted = false,
          isCallScreeningGranted = false,
          isNotificationListenerGranted = false,
          onRequestContacts = {},
          onRequestPhoneIdentity = {},
          onRequestCallScreening = {},
          onRequestNotificationListener = {},
          onEnterApp = {}
        )
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/onboarding_none_granted.png")
  }

  @Test
  fun onboarding_all_granted() {
    composeTestRule.setContent { 
      MyApplicationTheme { 
        AppOnboardingScreen(
          isContactsGranted = true,
          isPhoneNumbersGranted = true,
          isCallScreeningGranted = true,
          isNotificationListenerGranted = true,
          onRequestContacts = {},
          onRequestPhoneIdentity = {},
          onRequestCallScreening = {},
          onRequestNotificationListener = {},
          onEnterApp = {}
        )
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/onboarding_all_granted.png")
  }
}
