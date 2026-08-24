package de.herk.kfza

import de.herk.kfza.data.loader.DiplomaticPlateAssetLoader
import de.herk.kfza.data.loader.GeographicalPlateAssetLoader
import de.herk.kfza.data.loader.SpecialPlateAssetLoader
import de.herk.kfza.data.matcher.IdentifierMatcher
import de.herk.kfza.data.matcher.IdentifierNormalizer
import de.herk.kfza.data.model.PlateType
import de.herk.kfza.data.repository.InMemoryPlateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IdentifierInputBehaviorTest {
    private val dataDirectory = File("src/main/assets/data")
    private val regularEntries by lazy {
        File(dataDirectory, "plate_identifiers.json").inputStream().use(GeographicalPlateAssetLoader::parse)
    }
    private val specialEntries by lazy {
        File(dataDirectory, "special_identifiers.json").inputStream().use(SpecialPlateAssetLoader::parse)
    }
    private val diplomaticEntries by lazy {
        File(dataDirectory, "diplomatic_identifiers.json").inputStream().use(DiplomaticPlateAssetLoader::parse)
    }
    private val repository by lazy {
        InMemoryPlateRepository(
            rawEntries = regularEntries + specialEntries + diplomaticEntries,
            specialEntries = specialEntries,
            diplomaticEntries = diplomaticEntries
        )
    }
    private val matcher by lazy { IdentifierMatcher(repository) }

    @Test
    fun unknownCharactersRemainVisibleAndProduceNoMatch() {
        assertEquals("X", IdentifierNormalizer.uppercaseForDisplay("x"))
        assertFalse(matcher.match("X").isExact)
        assertFalse(matcher.match("X").isValidPrefix)
        assertEquals(null, repository.findByIdentifier("X"))
    }

    @Test
    fun oneTwoAndThreeLetterAustrianIdentifiersStillResolve() {
        assertResolvesExactly("A")
        assertResolvesExactly("BH")
        assertResolvesExactly("STD")
    }

    @Test
    fun authoritySpecialAndDiplomaticIdentifiersRemainReachable() {
        val authority = repository.findByIdentifier("EU")
        assertNotNull(authority)
        assertTrue(authority!!.types.contains(PlateType.GEOGRAPHICAL))

        val special = repository.findByIdentifier("A")
        assertNotNull(special)
        assertTrue(special!!.types.contains(PlateType.FEDERAL_ORGAN))

        val diplomatic = repository.findByIdentifier("CD")
        assertNotNull(diplomatic)
        assertTrue(diplomatic!!.types.contains(PlateType.DIPLOMATIC_VEHICLE_MARK))
    }

    @Test
    fun normalizationRemainsUppercaseWhitespaceInsensitiveAndTrimmed() {
        assertEquals("STD", IdentifierNormalizer.normalize("  s t d  "))
        assertEquals(repository.findByIdentifier("STD"), repository.findByIdentifier(" s t d "))
        assertTrue(matcher.match(" s t d ").isExact)
    }

    private fun assertResolvesExactly(identifier: String) {
        assertNotNull(identifier, repository.findByIdentifier(identifier))
        assertTrue(identifier, matcher.match(identifier).isExact)
        assertTrue(identifier, matcher.canAcceptInput(identifier))
    }
}
