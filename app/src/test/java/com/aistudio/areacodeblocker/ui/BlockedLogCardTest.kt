package com.aistudio.areacodeblocker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.data.entity.BlockedLog
import com.example.ui.components.BlockedLogCard
import com.example.ui.theme.MyApplicationTheme
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(BlockerTestRunner::class)
@Config(qualifiers = "en-rUS")
class BlockedLogCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `card displays timestamp in current phone timezone`() {
        composeTestRule.setContent {
            MyApplicationTheme {
                BlockedLogCard(
                    log = BlockedLog(
                        id = 1,
                        phoneNumber = "+15125550199",
                        areaCode = "512",
                        messageBody = null,
                        timestamp = Instant.parse("2024-01-01T00:30:00Z").toEpochMilli(),
                        type = "CALL",
                        senderName = null,
                    ),
                    locale = Locale.US,
                    zoneId = ZoneId.of("America/Los_Angeles"),
                    onDelete = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Dec 31, 2023 - 04:30 PM").assertIsDisplayed()
    }
}
