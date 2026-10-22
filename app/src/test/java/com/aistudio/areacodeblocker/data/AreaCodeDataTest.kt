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
        val us = AreaCodeData.countries.firstOrNull { (name) -> name == "United States" }
        assertNotNull(us)
        assertTrue(us!!.states.isNotEmpty())
        val alabama = us.states.find { (name) -> name == "Alabama" }
        assertNotNull(alabama)
        assertTrue(alabama!!.areaCodes.contains("205"))
    }

    @Test
    fun `US Territories are present and include Guam`() {
        val territories = AreaCodeData.countries.find { (name) -> name == "US Territories" }
        assertNotNull(territories)
        val guam = territories!!.states.find { (name) -> name == "Guam" }
        assertNotNull(guam)
        assertTrue(guam!!.areaCodes.contains("671"))
    }

    @Test
    fun `Federal Government is listed under Special Non-Geographic`() {
        val unitedStates = AreaCodeData.countries.first { (name) -> name == "United States" }
        assertFalse(unitedStates.states.any { (name) -> name == "Federal Government" })

        val special = AreaCodeData.countries.first { (name) -> name == "Special / Non-Geographic" }
        val federalGovernment = special.states.firstOrNull { (name) -> name == "Federal Government" }
        assertNotNull(federalGovernment)
        assertTrue(federalGovernment!!.areaCodes.contains("710"))
    }

    @Test
    fun `Other country is present and not supported`() {
        val other = AreaCodeData.countries.find { (name) -> name == "Other" }
        assertNotNull(other)
        assertFalse(other!!.isSupported)
    }
}
