package de.herk.kfza

import de.herk.kfza.data.loader.GeographicalPlateAssetLoader
import de.herk.kfza.data.loader.SpecialPlateAssetLoader
import de.herk.kfza.data.matcher.IdentifierMatcher
import de.herk.kfza.data.model.PlateEntry
import de.herk.kfza.data.model.PlateStatus
import de.herk.kfza.data.model.PlateType
import de.herk.kfza.data.repository.InMemoryPlateRepository
import de.herk.kfza.data.validation.PlateDatasetValidator
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

class AustrianSpecialIdentifierTest {
    private val dataDirectory = File("src/main/assets/data")
    private val regularEntries by lazy {
        File(dataDirectory, "plate_identifiers.json").inputStream().use(GeographicalPlateAssetLoader::parse)
    }
    private val specialEntries by lazy {
        File(dataDirectory, "special_identifiers.json").inputStream().use(SpecialPlateAssetLoader::parse)
    }
    private val repository by lazy {
        InMemoryPlateRepository(
            rawEntries = regularEntries + specialEntries,
            specialEntries = specialEntries
        )
    }
    private val matcher by lazy { IdentifierMatcher(repository) }

    @Test
    fun allSeventeenSpecialIdentifiersAreCurrentCompleteAndReachable() {
        assertEquals(17, specialEntries.size)
        assertEquals(17, specialEntries.map { it.identifier }.toSet().size)
        assertEquals(expected.keys, specialEntries.map { it.identifier }.toSet())

        specialEntries.forEach { entry ->
            val value = expected.getValue(entry.identifier)
            assertEquals(listOf(value.authorityName), entry.authorityNames)
            assertEquals(value.region, entry.region)
            assertEquals(value.type, entry.type)
            assertEquals(PlateStatus.ACTIVE, entry.status)
            assertTrue(entry.source?.isNotBlank() == true)
            assertTrue(matcher.match(entry.identifier).isExact)
            assertTrue(matcher.canAcceptInput(entry.identifier))

            val resolved = repository.findByIdentifier(entry.identifier)
            assertNotNull(entry.identifier, resolved)
            assertTrue(resolved!!.authorityNames.contains(value.authorityName))
            assertTrue(resolved.regions.contains(value.region))
            assertTrue(resolved.types.contains(value.type))
        }
    }

    @Test
    fun representativesResolveWithOfficialAustrianMeaning() {
        assertResolved("A", "Oberste Bundesorgane", "Österreich", PlateType.FEDERAL_ORGAN)
        assertResolved("N", "Landesorgane Niederösterreich", "Niederösterreich", PlateType.STATE_ORGAN)
        assertResolved("BH", "Bundesheer", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("BP", "Bundespolizei", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("FV", "Finanzverwaltung", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("FW", "Feuerwehr", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("JW", "Justizwache", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("BD", "Bundesbusdienst", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
        assertResolved("PT", "Post", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
    }

    @Test
    fun terminalAndExtendableSpecialIdentifiersFollowTheCombinedTrie() {
        val extendable = setOf("A", "B", "K", "N", "O", "S", "T", "V", "W")
        specialEntries.forEach { entry ->
            val match = matcher.match(entry.identifier)
            assertTrue(entry.identifier, match.isExact)
            assertEquals(entry.identifier, entry.identifier in extendable, match.hasLongerIdentifiers)
            assertEquals(entry.identifier, entry.identifier !in extendable, match.isTerminal)
            if (entry.identifier !in extendable) {
                assertFalse(entry.identifier, matcher.canAcceptInput(entry.identifier + "Q"))
            }
        }
    }

    @Test
    fun documentedSharedIdentifiersAreMergedWithSpecialMeaningFirst() {
        val expectedShared = setOf("B", "K", "S", "W")
        val report = PlateDatasetValidator.validate(
            entries = regularEntries + specialEntries,
            specialEntries = specialEntries,
            specialReachability = { matcher.canAcceptInput(it) }
        )
        assertEquals(expectedShared, report.duplicateIdentifiers)
        assertEquals(expectedShared, report.sharedSpecialIdentifiers)
        assertTrue(report.unresolvedDuplicateIdentifiers.isEmpty())
        assertTrue(report.specialDuplicateIdentifiers.isEmpty())

        expectedShared.forEach { identifier ->
            val special = specialEntries.single { it.identifier == identifier }
            val regular = regularEntries.single { it.identifier == identifier }
            val resolved = repository.findByIdentifier(identifier)!!
            assertEquals(special.type, resolved.types.first())
            assertEquals(special.authorityNames.single(), resolved.authorityNames.first())
            assertTrue(resolved.authorityNames.contains(regular.authorityNames.single()))
            assertTrue(resolved.types.contains(PlateType.GEOGRAPHICAL))
        }
    }

    @Test
    fun allRegularIdentifiersStillResolveAndUnsharedOnesRemainUnchanged() {
        val regularRepository = InMemoryPlateRepository(regularEntries)
        val shared = setOf("B", "K", "S", "W")
        assertEquals(98, regularEntries.size)
        regularEntries.forEach { regular ->
            assertEquals(regular, regularRepository.findByIdentifier(regular.identifier))
            val combined = repository.findByIdentifier(regular.identifier)
            assertNotNull(regular.identifier, combined)
            assertTrue(combined!!.authorityNames.containsAll(regular.authorityNames))
            assertTrue(combined.regions.contains(regular.region))
            if (regular.identifier !in shared) {
                assertEquals(regular, combined)
            }
        }
    }

    @Test
    fun unimportedDatasetsAndProductionTextRemainScopedAndUtf8() {
        assertEquals("[]", File(dataDirectory, "authority_series.json").readText(StandardCharsets.UTF_8))
        val productionData = dataDirectory.listFiles { file -> file.extension == "json" }!!
        productionData.forEach(::assertStrictUtf8)
        val combinedJson = productionData.joinToString("\n") { it.readText(StandardCharsets.UTF_8) }
        listOf("german_", "Bundesbehörde", "Bundeswehr", "Landkreis", "Kreisfreie Stadt", "Stadtstaat", "Städteregion").forEach {
            assertFalse("Non-Austrian production wording: $it", combinedJson.contains(it))
        }
        assertFalse("Non-Austrian production wording: Landespolizei", Regex("\\bLandespolizei\\b").containsMatchIn(combinedJson))
    }

    private fun assertResolved(identifier: String, authorityName: String, region: String, type: PlateType) {
        val entry = repository.findByIdentifier(identifier)
        assertNotNull(identifier, entry)
        assertTrue(entry!!.authorityNames.contains(authorityName))
        assertTrue(entry.regions.contains(region))
        assertTrue(entry.types.contains(type))
    }

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
        val type: PlateType
    )

    private val expected = mapOf(
        "A" to Expected("Oberste Bundesorgane", "Österreich", PlateType.FEDERAL_ORGAN),
        "B" to Expected("Landesorgane Burgenland", "Burgenland", PlateType.STATE_ORGAN),
        "K" to Expected("Landesorgane Kärnten", "Kärnten", PlateType.STATE_ORGAN),
        "N" to Expected("Landesorgane Niederösterreich", "Niederösterreich", PlateType.STATE_ORGAN),
        "O" to Expected("Landesorgane Oberösterreich", "Oberösterreich", PlateType.STATE_ORGAN),
        "S" to Expected("Landesorgane Salzburg", "Salzburg", PlateType.STATE_ORGAN),
        "ST" to Expected("Landesorgane Steiermark", "Steiermark", PlateType.STATE_ORGAN),
        "T" to Expected("Landesorgane Tirol", "Tirol", PlateType.STATE_ORGAN),
        "V" to Expected("Landesorgane Vorarlberg", "Vorarlberg", PlateType.STATE_ORGAN),
        "W" to Expected("Landesorgane Wien", "Wien", PlateType.STATE_ORGAN),
        "BD" to Expected("Bundesbusdienst", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "BH" to Expected("Bundesheer", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "BP" to Expected("Bundespolizei", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "FV" to Expected("Finanzverwaltung", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "FW" to Expected("Feuerwehr", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "JW" to Expected("Justizwache", "Österreich", PlateType.FEDERAL_SUBJECT_AREA),
        "PT" to Expected("Post", "Österreich", PlateType.FEDERAL_SUBJECT_AREA)
    )
}