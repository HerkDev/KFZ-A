package de.herk.kfza.data.repository

import android.content.Context
import de.herk.kfza.data.loader.AuthoritySeriesPlateAssetLoader
import de.herk.kfza.data.loader.DiplomaticPlateAssetLoader
import de.herk.kfza.data.loader.GeographicalPlateAssetLoader
import de.herk.kfza.data.loader.SpecialPlateAssetLoader
import de.herk.kfza.data.matcher.IdentifierMatcher
import de.herk.kfza.data.matcher.IdentifierNormalizer
import de.herk.kfza.data.model.PlateEntry
import de.herk.kfza.data.model.PlateType
import de.herk.kfza.data.validation.PlateDatasetValidator

interface PlateRepository {
    fun findByIdentifier(identifier: String): PlateEntry?
    fun identifiers(): Set<String>
    fun entries(): List<PlateEntry>
}

class GeographicalPlateRepository(context: Context) : PlateRepository {
    private val regularEntries = GeographicalPlateAssetLoader(context).load()
    private val specialEntries = SpecialPlateAssetLoader(context).load()
    private val diplomaticEntries = DiplomaticPlateAssetLoader(context).load()
    private val authoritySeriesEntries = AuthoritySeriesPlateAssetLoader(context).load()
    private val delegate = InMemoryPlateRepository(
        rawEntries = regularEntries + specialEntries + diplomaticEntries + authoritySeriesEntries,
        authoritySeriesEntries = authoritySeriesEntries,
        specialEntries = specialEntries,
        diplomaticEntries = diplomaticEntries
    )

    override fun findByIdentifier(identifier: String): PlateEntry? = delegate.findByIdentifier(identifier)
    override fun identifiers(): Set<String> = delegate.identifiers()
    override fun entries(): List<PlateEntry> = delegate.entries()
}

class InMemoryPlateRepository(
    private val rawEntries: List<PlateEntry>,
    private val authoritySeriesEntries: List<PlateEntry> = rawEntries.filter { it.type == PlateType.AUTHORITY_SERIES },
    private val specialEntries: List<PlateEntry> = emptyList(),
    private val diplomaticEntries: List<PlateEntry> = emptyList()
 ) : PlateRepository {
    private val entriesByIdentifier = rawEntries
        .groupBy { IdentifierNormalizer.normalize(it.identifier) }
        .mapValues { (_, entries) -> mergeEntries(entries) }
    private val canonicalEntries = entriesByIdentifier.values.toList()
    private val validation = PlateDatasetValidator.validate(
        entries = rawEntries,
        authoritySeriesEntries = authoritySeriesEntries,
        specialEntries = specialEntries,
        diplomaticEntries = diplomaticEntries,
        authoritySeriesReachability = { identifier -> IdentifierMatcher(this).canAcceptInput(identifier) },
        specialReachability = { identifier -> IdentifierMatcher(this).canAcceptInput(identifier) },
        diplomaticReachability = { identifier -> IdentifierMatcher(this).canAcceptInput(identifier) }
    )

    init {
        require(validation.unresolvedDuplicateIdentifiers.isEmpty()) { "Unresolved duplicate identifiers: ${validation.unresolvedDuplicateIdentifiers}" }
        require(validation.missingRequiredFields.isEmpty()) { "Missing required fields: ${validation.missingRequiredFields}" }
        require(validation.missingAuthorityTypes.isEmpty()) { "Geographical entries without authority type: ${validation.missingAuthorityTypes}" }
        require(validation.mismatchedAuthorityCounts.isEmpty()) { "Mismatched authority names: ${validation.mismatchedAuthorityCounts}" }
        require(validation.specialDuplicateIdentifiers.isEmpty()) { "Duplicate special identifiers: ${validation.specialDuplicateIdentifiers}" }
        require(validation.specialMissingSources.isEmpty()) { "Special identifiers without source: ${validation.specialMissingSources}" }
        require(validation.invalidSpecialTypes.isEmpty()) { "Invalid special identifier types: ${validation.invalidSpecialTypes}" }
        require(validation.specialNonCurrentIdentifiers.isEmpty()) { "Non-current special identifiers: ${validation.specialNonCurrentIdentifiers}" }
        require(validation.unreachableSpecialIdentifiers.isEmpty()) { "Unreachable special identifiers: ${validation.unreachableSpecialIdentifiers}" }
        require(validation.diplomaticDuplicateIdentifiers.isEmpty()) { "Duplicate diplomatic identifiers: ${validation.diplomaticDuplicateIdentifiers}" }
        require(validation.diplomaticMissingSources.isEmpty()) { "Diplomatic identifiers without source: ${validation.diplomaticMissingSources}" }
        require(validation.invalidDiplomaticTypes.isEmpty()) { "Invalid diplomatic identifier types: ${validation.invalidDiplomaticTypes}" }
        require(validation.diplomaticNonCurrentIdentifiers.isEmpty()) { "Non-current diplomatic identifiers: ${validation.diplomaticNonCurrentIdentifiers}" }
        require(validation.unreachableDiplomaticIdentifiers.isEmpty()) { "Unreachable diplomatic identifiers: ${validation.unreachableDiplomaticIdentifiers}" }
        require(validation.authoritySeriesMissingSources.isEmpty()) { "Authority series without source: ${validation.authoritySeriesMissingSources}" }
        require(validation.authoritySeriesMissingSourceTypes.isEmpty()) { "Authority series without source type: ${validation.authoritySeriesMissingSourceTypes}" }
        require(validation.authoritySeriesMissingAuthorityNames.isEmpty()) { "Authority series without authority name: ${validation.authoritySeriesMissingAuthorityNames}" }
        require(validation.authoritySeriesMissingRegions.isEmpty()) { "Authority series without region: ${validation.authoritySeriesMissingRegions}" }
        require(validation.invalidAuthoritySeriesTypes.isEmpty()) { "Invalid authority-series types: ${validation.invalidAuthoritySeriesTypes}" }
        require(validation.unreachableAuthoritySeriesIdentifiers.isEmpty()) { "Unreachable authority series: ${validation.unreachableAuthoritySeriesIdentifiers}" }
    }

    override fun findByIdentifier(identifier: String): PlateEntry? = entriesByIdentifier[IdentifierNormalizer.normalize(identifier)]
    override fun identifiers(): Set<String> = entriesByIdentifier.keys
    override fun entries(): List<PlateEntry> = canonicalEntries

    private fun mergeEntries(entries: List<PlateEntry>): PlateEntry {
        val orderedEntries = entries.sortedBy { entryPriority(it.type) }
        val lead = orderedEntries.first()
        return lead.copy(
            authorityNames = orderedEntries.flatMap { it.authorityNames }.distinct(),
            authorities = orderedEntries.flatMap { it.authorities }.distinct(),
            regions = orderedEntries.flatMap { it.regions }.distinct(),
            types = orderedEntries.flatMap { it.types }.distinct()
        )
    }

    private fun entryPriority(type: PlateType): Int = when (type) {
        PlateType.DIPLOMATIC_REGIONAL_SERIES,
        PlateType.CONSULAR_REGIONAL_SERIES,
        PlateType.DIPLOMATIC_VEHICLE_MARK,
        PlateType.CONSULAR_VEHICLE_MARK -> 0
        PlateType.FEDERAL_ORGAN,
        PlateType.STATE_ORGAN,
        PlateType.FEDERAL_SUBJECT_AREA -> 1
        PlateType.GEOGRAPHICAL -> 2
        else -> 1
    }
}