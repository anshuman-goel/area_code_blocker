package com.aistudio.areacodeblocker.data

import com.example.data.AreaCodeData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaCodeDataTest {

    @Test
    fun `countries list is populated`() {
        assertFalse(AreaCodeData.countries.isEmpty())
    }

    @Test
    fun `United States is present and has area codes`() {
        val us = AreaCodeData.countries.find { it.name == "United States" }
        assertNotNull(us)
        assertTrue(us!!.states.isNotEmpty())
        val alabama = us.states.find { it.name == "Alabama" }
        assertNotNull(alabama)
        assertTrue(alabama!!.areaCodes.contains("205"))
    }

    @Test
    fun `US Territories are present and include Guam`() {
        val territories = AreaCodeData.countries.find { it.name == "US Territories" }
        assertNotNull(territories)
        val guam = territories!!.states.find { it.name == "Guam" }
        assertNotNull(guam)
        assertTrue(guam!!.areaCodes.contains("671"))
    }

    @Test
    fun `Other country is present and not supported`() {
        val other = AreaCodeData.countries.find { it.name == "Other" }
        assertNotNull(other)
        assertFalse(other!!.isSupported)
    }
}
