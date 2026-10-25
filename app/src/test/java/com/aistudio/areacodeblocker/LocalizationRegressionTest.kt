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
      "The unqualified source locale must match Play Console's English (United States) source language",
      localeMetadata.lineSequence().any { it.trim() == "unqualifiedResLocale=en-US" },
    )
    assertFalse(
      "Generic English is ambiguous and can be mistaken for a Play translation target",
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
    val formatArgument = Regex("%(\\d+)\\$([a-zA-Z])")
    val resources = buildList {
      val strings = document.getElementsByTagName("string")
      addAll((0 until strings.length).map { strings.item(it) as Element })
      val plurals = document.getElementsByTagName("plurals")
      for (pluralIndex in 0 until plurals.length) {
        val plural = plurals.item(pluralIndex) as Element
        val items = plural.getElementsByTagName("item")
        addAll((0 until items.length).map { items.item(it) as Element })
      }
    }

    resources.forEach { resource ->
      if (resource.getAttribute("translatable") == "false") return@forEach
      val parent = resource.parentNode as? Element
      val name = resource.getAttribute("name").ifBlank { parent?.getAttribute("name").orEmpty() }
      val quantity = resource.getAttribute("quantity").takeIf(String::isNotBlank)
      val resourceLabel = if (quantity == null) name else "$name[$quantity]"
      val arguments = formatArgument.findAll(resource.textContent).map { match ->
        match.groupValues[1].toInt() to match.groupValues[2].lowercase()
      }.toList()

      val unprotected = (0 until resource.childNodes.length)
        .map { childIndex -> resource.childNodes.item(childIndex) }
        .filter { child -> child.nodeType == Node.TEXT_NODE }
        .flatMap { child -> formatArgument.findAll(child.nodeValue).map { it.value }.toList() }
      assertTrue(
        "Raw format arguments in $resourceLabel must be wrapped in xliff:g: $unprotected",
        unprotected.isEmpty(),
      )

      val xliffArguments = resource.getElementsByTagNameNS(
        "urn:oasis:names:tc:xliff:document:1.2",
        "g",
      )
      val protectedArguments = (0 until xliffArguments.length).flatMap { xliffIndex ->
        val placeholder = xliffArguments.item(xliffIndex) as Element
        assertTrue(
          "xliff:g in $resourceLabel needs a meaningful id",
          placeholder.getAttribute("id").isNotBlank(),
        )
        val placeholderArguments = formatArgument.findAll(placeholder.textContent).toList()
        assertEquals(
          "xliff:g in $resourceLabel must protect exactly one printf argument, not literal text",
          1,
          placeholderArguments.size,
        )
        placeholderArguments.map { match ->
          match.groupValues[1].toInt() to match.groupValues[2].lowercase()
        }
      }
      assertEquals(
        "Every format argument in $resourceLabel must be protected exactly once",
        arguments,
        protectedArguments,
      )

      val typesByPosition = arguments.groupBy({ it.first }, { it.second })
      typesByPosition.forEach { (position, types) ->
        assertEquals(
          "Format argument $position in $resourceLabel has conflicting types",
          1,
          types.distinct().size,
        )
      }
      if (arguments.isNotEmpty()) {
        assertEquals(
          "Format argument positions in $resourceLabel must be contiguous",
          (1..arguments.maxOf { it.first }).toSet(),
          arguments.map { it.first }.toSet(),
        )
      }
    }
  }

  @Test
  fun `translation source has no backslash escaped smart quotes`() {
    val strings = File(projectRoot, "app/src/main/res/values/strings.xml").readText()
    assertFalse("Smart opening quotes must not be backslash escaped", strings.contains("\\‘"))
    assertFalse("Smart closing quotes must not be backslash escaped", strings.contains("\\’"))
  }

  @Test
  fun `app name format strings supply the app name argument`() {
    val productionKotlin = File(projectRoot, "app/src/main/java").walkTopDown()
      .filter { it.extension == "kt" }
      .joinToString("\n") { it.readText() }
    listOf("notification_access_hint", "notification_access_settings_hint").forEach { name ->
      assertTrue(
        "$name must be formatted with app_name at its Compose call site",
        Regex(
          """stringResource\(\s*R\.string\.$name,\s*stringResource\(R\.string\.app_name\),?\s*\)""",
        ).containsMatchIn(productionKotlin),
      )
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
