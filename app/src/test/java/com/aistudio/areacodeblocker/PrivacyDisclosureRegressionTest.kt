package com.aistudio.areacodeblocker

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class PrivacyDisclosureRegressionTest {

    private val projectRoot = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) {
        it.parentFile
    }.first { File(it, "app/src/main/res").isDirectory }

    @Test
    fun `policy uses approved independent platform wording`() {
        val policy = File(projectRoot, "PRIVACY_POLICY.md").readText()

        assertTrue(
            policy.contains(
                "Your device’s operating system, app store, device manufacturer, mobile carrier, " +
                    "and related platform services—including Google services where applicable—may " +
                    "independently process installation, security, diagnostics, crash, or usage " +
                    "information under their own terms and privacy policies."
            )
        )
        assertTrue(
            policy.contains(
                "This statement does not mean that the App sends your contacts, caller details, " +
                    "message-notification content, rules, or blocked-event logs to those providers."
            )
        )
    }

    @Test
    fun `policy avoids unsupported privacy claims`() {
        val policy = File(projectRoot, "PRIVACY_POLICY.md").readText()

        listOf("Gemini", "RECEIVE_SMS", "physically impossible").forEach { unsupportedClaim ->
            assertFalse(
                "Privacy policy must not mention $unsupportedClaim",
                policy.contains(unsupportedClaim, ignoreCase = true),
            )
        }
    }

    @Test
    fun `legacy backup excludes databases and shared preferences`() {
        assertExcludesDatabaseAndSharedPreferences(
            File(projectRoot, "app/src/main/res/xml/backup_rules.xml"),
            sectionTag = "full-backup-content",
        )
    }

    @Test
    fun `cloud backup and device transfer exclude databases and shared preferences`() {
        val rulesFile = File(projectRoot, "app/src/main/res/xml/data_extraction_rules.xml")

        assertExcludesDatabaseAndSharedPreferences(rulesFile, sectionTag = "cloud-backup")
        assertExcludesDatabaseAndSharedPreferences(rulesFile, sectionTag = "device-transfer")
    }

    private fun assertExcludesDatabaseAndSharedPreferences(rulesFile: File, sectionTag: String) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rulesFile)
        val section = document.getElementsByTagName(sectionTag).item(0) as? Element
        assertTrue("Missing <$sectionTag> in ${rulesFile.name}", section != null)

        val excludedDomains = section!!
            .getElementsByTagName("exclude")
            .let { nodes ->
                (0 until nodes.length).map { index -> nodes.item(index) as Element }
            }
            .filter { it.getAttribute("path") == "." }
            .map { it.getAttribute("domain") }
            .toSet()

        assertTrue("$sectionTag must exclude the database domain", "database" in excludedDomains)
        assertTrue("$sectionTag must exclude the sharedpref domain", "sharedpref" in excludedDomains)
    }
}
