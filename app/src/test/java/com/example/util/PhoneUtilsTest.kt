package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneUtilsTest {

    @Test
    fun extractAreaCode_standard10Digit() {
        assertEquals("512", PhoneUtils.extractAreaCode("5125550199"))
        assertEquals("512", PhoneUtils.extractAreaCode("(512) 555-0199"))
    }

    @Test
    fun extractAreaCode_withCountryCode() {
        assertEquals("512", PhoneUtils.extractAreaCode("+15125550199"))
        assertEquals("512", PhoneUtils.extractAreaCode("15125550199"))
    }

    @Test
    fun extractAreaCode_shortNumber() {
        assertEquals("12", PhoneUtils.extractAreaCode("12"))
        assertEquals("123", PhoneUtils.extractAreaCode("123"))
    }

    @Test
    fun cleanNumber_removesFormatting() {
        assertEquals("15125550199", PhoneUtils.cleanNumber("+1 (512) 555-0199"))
        assertEquals("123", PhoneUtils.cleanNumber("abc 123 def"))
    }

    @Test
    fun extractAreaCode_edgeCases() {
        assertEquals("", PhoneUtils.extractAreaCode(""))
        assertEquals("800", PhoneUtils.extractAreaCode("800-FLOWERS"))
    }
}
