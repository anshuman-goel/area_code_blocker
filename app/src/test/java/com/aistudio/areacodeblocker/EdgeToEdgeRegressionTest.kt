package com.aistudio.areacodeblocker

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeToEdgeRegressionTest {

  private val projectRoot = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
    .first { File(it, "app/src/main").isDirectory }

  @Test
  fun `activity enables edge to edge before composing content`() {
    val activity = File(
      projectRoot,
      "app/src/main/java/com/example/MainActivity.kt",
    ).readText()

    val enableIndex = activity.indexOf("enableEdgeToEdge()")
    val contentIndex = activity.indexOf("setContent {")
    assertTrue("MainActivity must enable edge-to-edge", enableIndex >= 0)
    assertTrue("enableEdgeToEdge must run before setContent", enableIndex < contentIndex)
  }

  @Test
  fun `manifest uses adjust resize and modern theme`() {
    val manifest = File(projectRoot, "app/src/main/AndroidManifest.xml").readText()
    val theme = File(projectRoot, "app/src/main/res/values/themes.xml").readText()

    assertTrue(manifest.contains("android:windowSoftInputMode=\"adjustResize\""))
    assertFalse(theme.contains("Theme.DeviceDefault"))
    assertTrue(theme.contains("Theme.Material.Light.NoActionBar"))
  }

  @Test
  fun `sources avoid deprecated system bar APIs and opt out`() {
    val sourceRoots = listOf(
      File(projectRoot, "app/src/main"),
      File(projectRoot, "app/src/test"),
      File(projectRoot, "app/src/androidTest"),
    )
    val forbidden = listOf(
      "setStatusBarColor",
      "setNavigationBarColor",
      "windowOptOutEdgeToEdgeEnforcement",
      "android:statusBarColor",
      "android:navigationBarColor",
    )

    sourceRoots.asSequence()
      .flatMap { root -> root.walkTopDown().filter(File::isFile) }
      .filterNot { it.name == "EdgeToEdgeRegressionTest.kt" }
      .forEach { file ->
        val contents = file.readText()
        forbidden.forEach { token ->
          assertFalse("${file.relativeTo(projectRoot)} must not contain $token", contents.contains(token))
        }
      }
  }
}
