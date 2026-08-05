package de.herk.kfza

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EncodingQualityTest {
    private val sourceRoot = File("src")
    private val suspiciousFragments = listOf(
        Char(0x00C3).toString(),
        Char(0x00C2).toString(),
        Char(0x00E2).toString() + Char(0x20AC).toString(),
        Char(0xFFFD).toString()
    )

    @Test
    fun visibleStringsRemainValidUtf8() {
        val strings = File("src/main/res/values/strings.xml").readText(Charsets.UTF_8)
        val oe = Char(0x00F6)
        val ae = Char(0x00E4)
        val ue = Char(0x00FC)
        assertTrue(strings.contains("Bezirk / Beh" + oe + "rde"))
        val projectText = allTextFiles().joinToString("\n") { it.readText(Charsets.UTF_8) }
        assertTrue(projectText.contains("HerkDev"))
    }

    @Test
    fun projectSourceContainsNoKnownMojibakeFragments() {
        val matches = allTextFiles().flatMap { file ->
            suspiciousFragments.filter { fragment -> file.readText(Charsets.UTF_8).contains(fragment) }
                .map { fragment -> file.path + ": " + fragment }
        }
        assertTrue("Suspicious encoding fragments found: " + matches, matches.isEmpty())
    }

    @Test
    fun visibleLabelAndEmptyResultUseCorrectCharacters() {
        val strings = File("src/main/res/values/strings.xml").readText(Charsets.UTF_8)
        val emDash = Char(0x2014).toString()
        assertTrue(strings.contains("Bezirk / Beh" + Char(0x00F6) + "rde"))
        val value = Regex("""<string name="no_result_value">(.*?)</string>""").find(strings)?.groupValues?.get(1)
        assertEquals(emDash, value)
        assertFalse(value!!.contains(Char(0x00E2).toString()))
    }

    @Test
    fun notYetImportedDatasetsRemainExactlyEmptyArrays() {
        listOf(
            "authority_series.json"
        ).forEach { name ->
            assertEquals("[]", File("src/main/assets/data/$name").readText(Charsets.UTF_8))
        }
    }
    @Test
    fun finalAArtworkAndReferencesExist() {
        assertTrue(File("src/main/res/raw/nationality_a.svg").isFile)
        assertTrue(File("src/main/res/drawable/nationality_a.xml").isFile)
        val manifest = File("src/main/AndroidManifest.xml").readText(Charsets.UTF_8)
        assertTrue(manifest.contains("@mipmap/kfza_launcher"))
        assertTrue(manifest.contains("@mipmap/kfza_launcher_round"))
        assertTrue(File("src/main/res/drawable/splash_logo.xml").isFile)
        assertTrue(File("src/main/res/values-v31/themes.xml").readText(Charsets.UTF_8).contains("@drawable/splash_logo"))
        val mainSource = File("src/main/java/de/herk/kfza/ui/KfzaApp.kt").readText(Charsets.UTF_8)
        assertTrue(mainSource.contains("R.drawable.nationality_a"))
        assertFalse(mainSource.contains("nationality_" + "d"))
        assertTrue(File("src/main/res/values/themes.xml").readText(Charsets.UTF_8).contains("android:windowDisablePreview\">true"))
        assertTrue(File("src/main/res/values-v31/themes.xml").readText(Charsets.UTF_8).contains("android:windowDisablePreview\">true"))
        val oldName = "nationality_" + "d"
        assertFalse(allTextFiles().any { it.readText(Charsets.UTF_8).contains(oldName) })
        assertFalse(File("src/main/res/drawable/$oldName.xml").exists())
        assertFalse(File("src/main/res/raw/$oldName.svg").exists())
    }

    private fun allTextFiles(): List<File> =
        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in setOf("kt", "xml", "json", "svg", "properties", "gradle", "kts") }
            .toList()
}