package com.aistudio.areacodeblocker

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.w3c.dom.Element
import org.w3c.dom.Node
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
  fun `every translatable format argument is protected and structurally valid`() {
    val stringsFile = File(projectRoot, "app/src/main/res/values/strings.xml")
    val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
    val document = factory.newDocumentBuilder().parse(stringsFile)
    val strings = document.getElementsByTagName("string")
    val formatArgument = Regex("%(\\d+)\\$([a-zA-Z])")

    for (index in 0 until strings.length) {
      val string = strings.item(index) as Element
      if (string.getAttribute("translatable") == "false") continue
      val name = string.getAttribute("name")
      val arguments = formatArgument.findAll(string.textContent).map { match ->
        match.groupValues[1].toInt() to match.groupValues[2].lowercase()
      }.toList()

      val unprotected = (0 until string.childNodes.length).asSequence()
        .map { childIndex -> string.childNodes.item(childIndex) }
        .filter { child -> child.nodeType == Node.TEXT_NODE }
        .flatMap { child -> formatArgument.findAll(child.nodeValue).asSequence() }
        .map { match -> match.value }
        .toList()
      assertTrue("Raw format arguments in $name must be wrapped in xliff:g: $unprotected", unprotected.isEmpty())

      val xliffArguments = string.getElementsByTagNameNS(
        "urn:oasis:names:tc:xliff:document:1.2",
        "g",
      )
      val protectedArguments = (0 until xliffArguments.length).flatMap { xliffIndex ->
        val placeholder = xliffArguments.item(xliffIndex) as Element
        assertTrue("xliff:g in $name needs a meaningful id", placeholder.getAttribute("id").isNotBlank())
        formatArgument.findAll(placeholder.textContent).map { match ->
          match.groupValues[1].toInt() to match.groupValues[2].lowercase()
        }.toList()
      }
      assertEquals("Every format argument in $name must be protected exactly once", arguments, protectedArguments)

      val typesByPosition = arguments.groupBy({ it.first }, { it.second })
      typesByPosition.forEach { (position, types) ->
        assertEquals("Format argument $position in $name has conflicting types", 1, types.distinct().size)
      }
      if (arguments.isNotEmpty()) {
        assertEquals(
          "Format argument positions in $name must be contiguous",
          (1..arguments.maxOf { it.first }).toSet(),
          arguments.map { it.first }.toSet(),
        )
      }
    }
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
