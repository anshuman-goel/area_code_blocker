package com.aistudio.areacodeblocker.service

import com.example.service.SmsNotificationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsNotificationFilterTest {

    @Test
    fun `blocked area code matches formatted SMS sender number`() {
        val match = SmsNotificationFilter.findMatch(
            senderIdentifiers = listOf("+1 (512) 555-0199"),
            messageText = "Hello",
            blockedAreaCodes = listOf("512"),
            blockedKeywords = emptyList(),
        )

        assertEquals("512", match?.areaCode)
        assertNull(match?.keyword)
    }

    @Test
    fun `MessagingStyle sender number matches when conversation title is not a number`() {
        val match = SmsNotificationFilter.findMatch(
            senderIdentifiers = listOf("Weekend group", "+1 307 555 0199"),
            messageText = "Hello",
            blockedAreaCodes = listOf("307"),
            blockedKeywords = emptyList(),
        )

        assertEquals("307", match?.areaCode)
    }

    @Test
    fun `short code is not treated as an area code phone number`() {
        val match = SmsNotificationFilter.findMatch(
            senderIdentifiers = listOf("51255"),
            messageText = "Account notice",
            blockedAreaCodes = listOf("512"),
            blockedKeywords = emptyList(),
        )

        assertNull(match)
    }

    @Test
    fun `keyword matching remains supported`() {
        val match = SmsNotificationFilter.findMatch(
            senderIdentifiers = listOf("Unknown sender"),
            messageText = "Claim your lottery prize",
            blockedAreaCodes = emptyList(),
            blockedKeywords = listOf("lottery"),
        )

        assertEquals("lottery", match?.keyword)
    }
}
