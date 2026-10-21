package com.aistudio.areacodeblocker.ui

import com.example.ui.components.formatLogTimestamp
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockedLogTimestampFormatterTest {
    private val timestamp = Instant.parse("2024-01-01T00:30:00Z").toEpochMilli()

    @Test
    fun `formats timestamp in UTC with US locale and 12 hour clock`() {
        assertEquals(
            "Jan 01, 2024 - 12:30 AM",
            formatLogTimestamp(timestamp, Locale.US, ZoneId.of("UTC")),
        )
    }

    @Test
    fun `formats same timestamp in device zone including previous date rollover`() {
        assertEquals(
            "Dec 31, 2023 - 04:30 PM",
            formatLogTimestamp(timestamp, Locale.US, ZoneId.of("America/Los_Angeles")),
        )
    }
}
