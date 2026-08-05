package de.herk.kfza

import de.herk.kfza.data.loader.GeographicalPlateAssetLoader
import de.herk.kfza.data.matcher.IdentifierMatcher
import de.herk.kfza.data.model.GeographicalAuthorityType
import de.herk.kfza.data.model.PlateStatus
import de.herk.kfza.data.model.PlateType
import de.herk.kfza.data.repository.InMemoryPlateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

class AustrianRegularIdentifierTest {
    private val dataDirectory = File("src/main/assets/data")
    private val entries by lazy {
        File(dataDirectory, "plate_identifiers.json").inputStream().use(GeographicalPlateAssetLoader::parse)
    }
    private val repository by lazy { InMemoryPlateRepository(entries) }
    private val matcher by lazy { IdentifierMatcher(repository) }
    private val validAuthorityTypes = setOf(
        GeographicalAuthorityType.DISTRICT_AUTHORITY,
        GeographicalAuthorityType.STATE_POLICE_DIRECTORATE,
        GeographicalAuthorityType.MAGISTRATE,
        GeographicalAuthorityType.EXPOSITURE
    )

    @Test
    fun allNinetyEightCurrentRegularIdentifiersAreCompleteUniqueAndReachable() {
        assertEquals(98, entries.size)
        assertEquals(98, entries.map { it.identifier }.toSet().size)
        assertEquals(
            mapOf(
                "Burgenland" to 8,
                "Kärnten" to 10,
                "Niederösterreich" to 26,
                "Oberösterreich" to 18,
                "Salzburg" to 6,
                "Steiermark" to 16,
                "Tirol" to 9,
                "Vorarlberg" to 4,
                "Wien" to 1
            ),
            entries.groupingBy { it.region }.eachCount()
        )

        entries.forEach { entry ->
            assertTrue(entry.identifier.isNotBlank())
            assertTrue(entry.authorityNames.isNotEmpty())
            assertTrue(entry.authorityNames.all { it.isNotBlank() })
            assertTrue(entry.region.isNotBlank())
            assertEquals(PlateType.GEOGRAPHICAL, entry.type)
            assertEquals(PlateStatus.ACTIVE, entry.status)
            assertNotNull(entry.source)
            assertTrue(entry.source!!.isNotBlank())
            assertEquals(1, entry.authorities.size)
            assertEquals(entry.authorityNames, entry.authorities.map { it.name })
            assertTrue(entry.authorities.all { it.authorityType in validAuthorityTypes })
            assertEquals(entry, repository.findByIdentifier(entry.identifier))
            assertTrue(matcher.match(entry.identifier).isExact)
            assertTrue(matcher.canAcceptInput(entry.identifier))
        }
    }

    @Test
    fun stateCapitalsAndNamedRepresentativeAuthoritiesResolveExactly() {
        val expected = mapOf(
            "W" to Expected("Landespolizeidirektion Wien", "Wien", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "G" to Expected("Landespolizeidirektion Steiermark für Graz", "Steiermark", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "L" to Expected("Landespolizeidirektion Oberösterreich für Linz", "Oberösterreich", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "S" to Expected("Landespolizeidirektion Salzburg für Salzburg", "Salzburg", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "I" to Expected("Landespolizeidirektion Tirol für Innsbruck", "Tirol", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "K" to Expected("Landespolizeidirektion Kärnten für Klagenfurt am Wörthersee", "Kärnten", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "B" to Expected("BH Bregenz", "Vorarlberg", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "P" to Expected("Landespolizeidirektion Niederösterreich für St. Pölten", "Niederösterreich", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "E" to Expected("Landespolizeidirektion Burgenland für Eisenstadt und Rust", "Burgenland", GeographicalAuthorityType.STATE_POLICE_DIRECTORATE),
            "EU" to Expected("BH Eisenstadt", "Burgenland", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "FE" to Expected("BH Feldkirchen", "Kärnten", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "AM" to Expected("BH Amstetten", "Niederösterreich", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "BR" to Expected("BH Braunau am Inn", "Oberösterreich", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "HA" to Expected("BH Hallein", "Salzburg", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "BM" to Expected("BH Bruck-Mürzzuschlag", "Steiermark", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "IL" to Expected("BH Innsbruck", "Tirol", GeographicalAuthorityType.DISTRICT_AUTHORITY),
            "B" to Expected("BH Bregenz", "Vorarlberg", GeographicalAuthorityType.DISTRICT_AUTHORITY)
        )

        expected.forEach { (identifier, value) ->
            val entry = repository.findByIdentifier(identifier)
            assertNotNull(identifier, entry)
            assertEquals(value.authorityName, entry!!.authorityNames.single())
            assertEquals(value.region, entry.region)
            assertEquals(value.authorityType, entry.authorities.single().authorityType)
            assertTrue(matcher.match(identifier).isExact)
        }

        // Wien has no politische Bezirkshauptmannschaft in the current official table.
        assertEquals(setOf("Burgenland", "Kärnten", "Niederösterreich", "Oberösterreich", "Salzburg", "Steiermark", "Tirol", "Vorarlberg"),
            entries.filter { it.authorities.single().authorityType == GeographicalAuthorityType.DISTRICT_AUTHORITY }
                .map { it.region }
                .toSet())
    }

    @Test
    fun exactTerminalAndExtendablePrefixesBehaveCorrectly() {
        assertTrue(matcher.match("EU").isTerminal)
        assertFalse(matcher.canAcceptInput("EUQ"))

        listOf("B", "K", "L", "S", "W").forEach { identifier ->
            val match = matcher.match(identifier)
            assertTrue("$identifier must be an exact identifier", match.isExact)
            assertTrue("$identifier must remain extendable", match.hasLongerIdentifiers)
            assertFalse("$identifier must not be terminal", match.isTerminal)
        }
    }

    @Test
    fun labelsDatasetsAndUtf8RemainAustrianAndScoped() {
        val strings = File("src/main/res/values/strings.xml").readText(StandardCharsets.UTF_8)
        assertEquals("Bezirk / Behörde", stringValue(strings, "result_authority"))
        assertEquals("Bundesland", stringValue(strings, "result_state"))
        assertEquals("Typ", stringValue(strings, "result_type"))
        assertFalse(strings.contains("Landkreis / Behörde"))

        val regularJson = File(dataDirectory, "plate_identifiers.json").readText(StandardCharsets.UTF_8)
        listOf("Landkreis", "Kreisfreie Stadt", "Stadtstaat", "Städteregion").forEach {
            assertFalse("German authority type remains in production data: $it", regularJson.contains(it))
        }
        listOf("authority_series.json").forEach { name ->
            assertEquals("[]", File(dataDirectory, name).readText(StandardCharsets.UTF_8))
        }
        dataDirectory.listFiles { file -> file.extension == "json" }!!.forEach(::assertStrictUtf8)
    }

    private fun stringValue(xml: String, name: String): String =
        Regex("""<string name="$name">(.*?)</string>""").find(xml)?.groupValues?.get(1)
            ?: error("Missing string resource: $name")

    private fun assertStrictUtf8(file: File) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(file.readBytes()))
        } catch (exception: CharacterCodingException) {
            throw AssertionError("Invalid UTF-8 in ${file.path}", exception)
        }
    }

    private data class Expected(
        val authorityName: String,
        val region: String,
        val authorityType: GeographicalAuthorityType
    )
}