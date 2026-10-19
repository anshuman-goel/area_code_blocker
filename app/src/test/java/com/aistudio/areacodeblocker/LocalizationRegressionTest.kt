package com.aistudio.areacodeblocker

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationRegressionTest {

  private val projectRoot = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
    .first { File(it, "app/src/main/res").isDirectory }

  @Test
  fun `bundle does not constrain locales supplied by Play automatic translation`() {
    val buildFile = File(projectRoot, "app/build.gradle.kts").readText()
    assertFalse(
      "localeFilters removes every locale except its allowlist from packaged resources",
      buildFile.contains("localeFilters"),
    )
    assertFalse(
      "A generated LocaleConfig advertises only locales already compiled into the upload",
      buildFile.contains("generateLocaleConfig = true"),
    )
    assertTrue(
      "Language splitting must be disabled so translated resources remain in the base APK",
      Regex("""bundle\s*\{[\s\S]*?language\s*\{[\s\S]*?enableSplit\s*=\s*false""")
        .containsMatchIn(buildFile),
    )
    val localeMetadata = File(projectRoot, "app/src/main/res/resources.properties").readText()
    assertTrue(
      "The unqualified source strings use Play's generic English source language",
      localeMetadata.lineSequence().any { it.trim() == "unqualifiedResLocale=en" },
    )
  }

  @Test
  fun `app does not claim bundled translations`() {
    val translatedValuesDirectory = Regex("values-(?!night(?:-|$)|v\\d+(?:-|$)).+")
    val translatedDirectories = File(projectRoot, "app/src/main/res").listFiles().orEmpty()
      .filter { it.isDirectory && translatedValuesDirectory.matches(it.name) }
    assertTrue(
      "Play automatic translations must not be advertised as app-provided resources: $translatedDirectories",
      translatedDirectories.isEmpty(),
    )
  }

  @Test
  fun `key user visible strings are translatable resources`() {
    val stringsFile = File(projectRoot, "app/src/main/res/values/strings.xml")
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringsFile)
    val resources = (0 until document.getElementsByTagName("string").length).associate { index ->
      val node = document.getElementsByTagName("string").item(index)
      node.attributes.getNamedItem("name").nodeValue to node.textContent
    }
    listOf(
      "activate_spam_shield",
      "important_call_blocking_notice",
      "filtered_operations_log",
      "block_by_region",
      "blocked_sms_keywords",
    ).forEach { assertTrue("Missing translatable string: $it", resources[it].orEmpty().isNotBlank()) }

    val productionKotlin = File(projectRoot, "app/src/main/java").walkTopDown()
      .filter { it.extension == "kt" }
      .joinToString("\n") { it.readText() }
    resources.values.filter { it.length >= 12 && !it.contains('%') }.forEach { value ->
      assertFalse("Resource text is still hardcoded in Kotlin: $value", productionKotlin.contains("\"$value\""))
    }
  }
}
