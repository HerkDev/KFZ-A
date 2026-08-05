package de.herk.kfza

import de.herk.kfza.data.matcher.IdentifierMatcher
import de.herk.kfza.data.model.GeographicalAuthority
import de.herk.kfza.data.model.GeographicalAuthorityType
import de.herk.kfza.data.model.PlateEntry
import de.herk.kfza.data.model.PlateStatus
import de.herk.kfza.data.repository.InMemoryPlateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NeutralArchitectureTest {
    private val repository = InMemoryPlateRepository(
        listOf(
            entry("QZ"),
            entry("QZX")
        )
    )
    private val matcher = IdentifierMatcher(repository)

    @Test
    fun exactLookupAndOptionalSpaceNormalizationRemainGeneric() {
        assertEquals(repository.findByIdentifier("QZ"), repository.findByIdentifier("Q Z"))
        assertTrue(matcher.match("QZ").isExact)
        assertTrue(matcher.match("Q Z").isExact)
    }

    @Test
    fun leadingTrailingAndRepeatedSpacesRemainAcceptedAroundTerminalIdentifiers() {
        assertTrue(matcher.match(" QZ ").isExact)
        assertTrue(matcher.match("Q  Z").isExact)
        assertEquals(repository.findByIdentifier("QZ"), repository.findByIdentifier(" QZ "))
    }

    @Test
    fun triePrefixAndTerminalDetectionRemainGeneric() {
        assertTrue(matcher.canAcceptInput("Q"))
        assertTrue(matcher.match("QZ").hasLongerIdentifiers)
        assertFalse(matcher.match("QZ").isTerminal)
        assertTrue(matcher.match("QZX").isTerminal)
        assertFalse(matcher.canAcceptInput("QZY"))
    }

    private fun entry(identifier: String) = PlateEntry(
        identifier = identifier,
        authorityNames = listOf("Sample Authority"),
        region = "Sample Region",
        status = PlateStatus.ACTIVE,
        authorities = listOf(
            GeographicalAuthority("Sample Authority", GeographicalAuthorityType.DISTRICT_AUTHORITY)
        )
    )
}