package com.aistudio.areacodeblocker

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationRegressionTest {

  private val projectRoot = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
    .first { File(it, "app/src/main/res").isDirectory }

  @Test
  fun `default resource locale is American English`() {
    val metadata = File(projectRoot, "app/src/main/res/resources.properties")
    assertTrue("resources.properties must exist", metadata.isFile)
    val properties = java.util.Properties().apply { metadata.inputStream().use(::load) }
    assertEquals("en-US", properties.getProperty("unqualifiedResLocale"))
  }

  @Test
  fun `app does not provide Norwegian resources`() {
    val res = File(projectRoot, "app/src/main/res")
    assertFalse(
      "Norwegian must not be advertised as an app-provided locale",
      res.listFiles().orEmpty().any { it.isDirectory && Regex("values-(no|nb|nn)(-.*)?").matches(it.name) },
    )
    val buildFile = File(projectRoot, "app/build.gradle.kts").readText()
    assertTrue(buildFile.contains("generateLocaleConfig = true"))
    assertTrue(buildFile.contains("localeFilters += \"en\""))
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
