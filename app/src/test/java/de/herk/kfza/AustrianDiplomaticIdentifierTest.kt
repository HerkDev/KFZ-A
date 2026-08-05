package de.herk.kfza

import de.herk.kfza.data.loader.DiplomaticPlateAssetLoader
import de.herk.kfza.data.loader.GeographicalPlateAssetLoader
import de.herk.kfza.data.loader.SpecialPlateAssetLoader
import de.herk.kfza.data.matcher.IdentifierMatcher
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
import java.nio.charset.StandardCharsets

class AustrianDiplomaticIdentifierTest {
    private val dataDirectory = File("src/main/assets/data")
    private val regularEntries by lazy { File(dataDirectory, "plate_identifiers.json").inputStream().use(GeographicalPlateAssetLoader::parse) }
    private val specialEntries by lazy { File(dataDirectory, "special_identifiers.json").inputStream().use(SpecialPlateAssetLoader::parse) }
    private val diplomaticEntries by lazy { File(dataDirectory, "diplomatic_identifiers.json").inputStream().use(DiplomaticPlateAssetLoader::parse) }
    private val repository by lazy {
        InMemoryPlateRepository(
            rawEntries = regularEntries + specialEntries + diplomaticEntries,
            specialEntries = specialEntries,
            diplomaticEntries = diplomaticEntries
        )
    }
    private val matcher by lazy { IdentifierMatcher(repository) }
    private val expectedDiplomatic = setOf(
        "BD", "KD", "ND", "OD", "SD", "STD", "TD", "VD", "WD", "GD",
        "BK", "KK", "NK", "OK", "SK", "STK", "TK", "VK", "WK", "GK",
        "CD", "CC"
    )

    @Test
    fun allDocumentedDiplomaticAndConsularIdentifiersResolve() {
        assertEquals(22, diplomaticEntries.size)
        assertEquals(expectedDiplomatic, diplomaticEntries.map { it.identifier }.toSet())
        assertEquals(22, diplomaticEntries.map { it.identifier }.distinct().size)
        diplomaticEntries.forEach { entry ->
            assertTrue(entry.identifier.isNotBlank())
            assertTrue(entry.authorityNames.isNotEmpty())
            assertTrue(entry.authorityNames.all(String::isNotBlank))
            assertTrue(entry.region.isNotBlank())
            assertEquals(PlateStatus.ACTIVE, entry.status)
            assertTrue(entry.source?.isNotBlank() == true)
            assertTrue(matcher.match(entry.identifier).isExact)
            assertTrue(matcher.canAcceptInput(entry.identifier))
            val resolved = repository.findByIdentifier(entry.identifier)
            assertNotNull(entry.identifier, resolved)
            assertTrue(resolved!!.authorityNames.containsAll(entry.authorityNames))
            assertTrue(resolved.regions.contains(entry.region))
            assertTrue(resolved.types.contains(entry.type))
        }
    }

    @Test
    fun documentedTypesAndRegionsArePreserved() {
        assertResolved("CD", "corps diplomatique", "Österreich", PlateType.DIPLOMATIC_VEHICLE_MARK)
        assertResolved("CC", "corps consulaire", "Österreich", PlateType.CONSULAR_VEHICLE_MARK)
        assertResolved("WD", "Diplomatisches Personal / gleichgestellte internationale Organisationen, Wien", "Wien", PlateType.DIPLOMATIC_REGIONAL_SERIES)
        assertResolved("WK", "Konsularkorps, Wien", "Wien", PlateType.CONSULAR_REGIONAL_SERIES)
        assertResolved("GD", "Diplomatisches Personal / gleichgestellte internationale Organisationen, Graz", "Graz", PlateType.DIPLOMATIC_REGIONAL_SERIES)
        assertResolved("GK", "Konsularkorps, Graz", "Graz", PlateType.CONSULAR_REGIONAL_SERIES)
    }

    @Test
    fun prefixAndTerminalBehaviourMatchesTheDocumentedStructure() {
        assertTrue(matcher.match("ST").isExact)
        assertTrue(matcher.match("ST").hasLongerIdentifiers)
        assertTrue(matcher.match("STD").isTerminal)
        assertTrue(matcher.match("STK").isTerminal)
        assertTrue(matcher.match("CD").isTerminal)
        assertTrue(matcher.match("CC").isTerminal)
        assertFalse(matcher.canAcceptInput("CDQ"))
        assertFalse(matcher.canAcceptInput("STDQ"))
        assertFalse(matcher.canAcceptInput("STKQ"))
        assertTrue(matcher.canAcceptInput("w d"))
    }

    @Test
    fun documentedCrossCategoryCollisionsAreExplicitlyResolved() {
        val report = PlateDatasetValidator.validate(
            entries = regularEntries + specialEntries + diplomaticEntries,
            specialEntries = specialEntries,
            diplomaticEntries = diplomaticEntries,
            specialReachability = { matcher.canAcceptInput(it) },
            diplomaticReachability = { matcher.canAcceptInput(it) }
        )
        assertEquals(setOf("B", "K", "S", "W", "BD", "ND", "VK", "GD", "NK", "SD"), report.duplicateIdentifiers)
        assertEquals(setOf("B", "K", "S", "W"), report.sharedSpecialIdentifiers)
        assertEquals(setOf("BD", "ND", "VK", "GD", "NK", "SD"), report.sharedDiplomaticIdentifiers)
        assertTrue(report.diplomaticDuplicateIdentifiers.isEmpty())
        assertTrue(report.unresolvedDuplicateIdentifiers.isEmpty())
        val bd = repository.findByIdentifier("BD")!!
        assertEquals(PlateType.DIPLOMATIC_REGIONAL_SERIES, bd.types.first())
        assertTrue(bd.authorityNames.first().startsWith("Diplomatisches Personal"))
        assertTrue(bd.authorityNames.contains("Bundesbusdienst"))
    }

    @Test
    fun regularAndSpecialDatasetsRemainIntact() {
        assertEquals(98, regularEntries.size)
        assertEquals(17, specialEntries.size)
        regularEntries.forEach { assertNotNull(it.identifier, repository.findByIdentifier(it.identifier)) }
        specialEntries.forEach {
            val resolved = repository.findByIdentifier(it.identifier)
            assertNotNull(it.identifier, resolved)
            assertTrue(resolved!!.authorityNames.contains(it.authorityNames.single()))
            assertTrue(resolved.types.contains(it.type))
        }
        assertEquals("[]", File(dataDirectory, "authority_series.json").readText(StandardCharsets.UTF_8))
    }

    private fun assertResolved(identifier: String, authority: String, region: String, type: PlateType) {
        val entry = repository.findByIdentifier(identifier)
        assertNotNull(identifier, entry)
        assertTrue(entry!!.authorityNames.contains(authority))
        assertTrue(entry.regions.contains(region))
        assertTrue(entry.types.contains(type))
    }
}