package de.herk.kfza.data.validation

import de.herk.kfza.data.matcher.IdentifierNormalizer
import de.herk.kfza.data.model.PlateEntry
import de.herk.kfza.data.model.PlateStatus
import de.herk.kfza.data.model.PlateType

object PlateDatasetValidator {
    fun validate(
        entries: List<PlateEntry>,
        authoritySeriesEntries: List<PlateEntry> = entries.filter { it.type == PlateType.AUTHORITY_SERIES },
        specialEntries: List<PlateEntry> = emptyList(),
        diplomaticEntries: List<PlateEntry> = emptyList(),
        authoritySeriesReachability: (String) -> Boolean = { true },
        specialReachability: (String) -> Boolean = { true },
        diplomaticReachability: (String) -> Boolean = { true }
    ): PlateDatasetReport {
        val entriesByIdentifier = entries.groupBy { IdentifierNormalizer.normalize(it.identifier) }
        val duplicateIdentifiers = entriesByIdentifier.filterValues { it.size > 1 }.keys
        val specialByIdentifier = specialEntries.groupBy { IdentifierNormalizer.normalize(it.identifier) }
        val diplomaticByIdentifier = diplomaticEntries.groupBy { IdentifierNormalizer.normalize(it.identifier) }
        val sharedSpecialIdentifiers = duplicateIdentifiers.filter { identifier ->
            val group = entriesByIdentifier.getValue(identifier)
            specialByIdentifier[identifier]?.singleOrNull()?.type == PlateType.STATE_ORGAN &&
                group.size == 2 && group.any { it.type == PlateType.GEOGRAPHICAL }
        }.toSet()
        val sharedDiplomaticIdentifiers = duplicateIdentifiers.filter { identifier ->
            val group = entriesByIdentifier.getValue(identifier)
            diplomaticByIdentifier[identifier]?.singleOrNull() != null && group.size >= 2
        }.toSet()
        val unresolvedDuplicateIdentifiers = duplicateIdentifiers - sharedSpecialIdentifiers - sharedDiplomaticIdentifiers
        val missingRequiredFields = entries.filter { entry ->
            entry.identifier.isBlank() || entry.authorityNames.isEmpty() ||
                entry.authorityNames.any(String::isBlank) || entry.region.isBlank()
        }.map { it.identifier }.toSet()
        val missingAuthorityTypes = entries.filter { entry ->
            entry.type == PlateType.GEOGRAPHICAL &&
                (entry.authorities.isEmpty() || entry.authorities.any { it.name.isBlank() })
        }.map { it.identifier }.toSet()
        val mismatchedAuthorityCounts = entries.filter { entry ->
            entry.type == PlateType.GEOGRAPHICAL && entry.authorities.size != entry.authorityNames.size
        }.map { it.identifier }.toSet()
        val legacyDiplomaticEntries = entries.filter {
            it.type == PlateType.DIPLOMATIC_CORPS || it.type == PlateType.INTERNATIONAL_ORGANISATION
        }
        val diplomaticDuplicateIdentifiers = diplomaticEntries.groupingBy { IdentifierNormalizer.normalize(it.identifier) }
            .eachCount().filterValues { it > 1 }.keys
        val validDiplomaticTypes = setOf(
            PlateType.DIPLOMATIC_REGIONAL_SERIES,
            PlateType.CONSULAR_REGIONAL_SERIES,
            PlateType.DIPLOMATIC_VEHICLE_MARK,
            PlateType.CONSULAR_VEHICLE_MARK
        )
        val diplomaticMissingSources = diplomaticEntries.filter { it.source.isNullOrBlank() }
            .map { it.identifier }.toSet()
        val invalidDiplomaticTypes = diplomaticEntries.filter { it.type !in validDiplomaticTypes }
            .map { it.identifier }.toSet()
        val diplomaticNonCurrentIdentifiers = diplomaticEntries.filter { it.status != PlateStatus.ACTIVE }
            .map { it.identifier }.toSet()
        val unreachableDiplomaticIdentifiers = diplomaticEntries.filter { !diplomaticReachability(it.identifier) }
            .map { it.identifier }.toSet()
        val specialDuplicateIdentifiers = specialEntries.groupingBy { IdentifierNormalizer.normalize(it.identifier) }.eachCount()
            .filterValues { it > 1 }.keys
        val validSpecialTypes = setOf(
            PlateType.FEDERAL_ORGAN,
            PlateType.STATE_ORGAN,
            PlateType.FEDERAL_SUBJECT_AREA
        )
        val specialMissingSources = specialEntries.filter { it.source.isNullOrBlank() }
            .map { it.identifier }.toSet()
        val invalidSpecialTypes = specialEntries.filter { it.type !in validSpecialTypes }
            .map { it.identifier }.toSet()
        val specialNonCurrentIdentifiers = specialEntries.filter { it.status != PlateStatus.ACTIVE }
            .map { it.identifier }.toSet()
        val unreachableSpecialIdentifiers = specialEntries.filter { !specialReachability(it.identifier) }
            .map { it.identifier }.toSet()
        val authoritySeriesMissingSources = authoritySeriesEntries.filter { it.source.isNullOrBlank() }
            .map { it.identifier }.toSet()
        val authoritySeriesMissingSourceTypes = authoritySeriesEntries.filter { it.sourceType == null }
            .map { it.identifier }.toSet()
        val authoritySeriesMissingAuthorityNames = authoritySeriesEntries.filter {
            it.authorityNames.isEmpty() || it.authorityNames.any(String::isBlank)
        }.map { it.identifier }.toSet()
        val authoritySeriesMissingRegions = authoritySeriesEntries.filter { it.region.isBlank() }
            .map { it.identifier }.toSet()
        val validAuthoritySeriesTypes = setOf(
            PlateType.AUTHORITY_SERIES,
            PlateType.FINANCE_ADMINISTRATION,
            PlateType.CONSTITUTIONAL_COURT
        )
        val invalidAuthoritySeriesTypes = authoritySeriesEntries.filter { it.type !in validAuthoritySeriesTypes }
            .map { it.identifier }.toSet()
        val unreachableAuthoritySeriesIdentifiers = authoritySeriesEntries.filter {
            !authoritySeriesReachability(it.identifier)
        }.map { it.identifier }.toSet()

        return PlateDatasetReport(
            totalCount = entries.size,
            countByStatus = PlateStatus.entries.associateWith { status -> entries.count { it.status == status } },
            countByRegion = entries.groupingBy { it.region }.eachCount(),
            duplicateIdentifiers = duplicateIdentifiers,
            unresolvedDuplicateIdentifiers = unresolvedDuplicateIdentifiers,
            sharedSpecialIdentifiers = sharedSpecialIdentifiers,
            sharedDiplomaticIdentifiers = sharedDiplomaticIdentifiers,
            missingRequiredFields = missingRequiredFields,
            missingAuthorityTypes = missingAuthorityTypes,
            mismatchedAuthorityCounts = mismatchedAuthorityCounts,
            diplomaticIdentifierCount = diplomaticEntries.size + legacyDiplomaticEntries.size,
            diplomaticCountryCodeCount = diplomaticEntries.count { it.type == PlateType.DIPLOMATIC_VEHICLE_MARK } + legacyDiplomaticEntries.count { it.type == PlateType.DIPLOMATIC_CORPS },
            diplomaticOrganisationCodeCount = diplomaticEntries.count { it.type == PlateType.CONSULAR_VEHICLE_MARK } + legacyDiplomaticEntries.count { it.type == PlateType.INTERNATIONAL_ORGANISATION },
            diplomaticMissingSources = diplomaticMissingSources + legacyDiplomaticEntries.filter { it.source.isNullOrBlank() }.map { it.identifier },
            diplomaticDuplicateIdentifiers = diplomaticDuplicateIdentifiers,
            invalidDiplomaticTypes = invalidDiplomaticTypes,
            diplomaticNonCurrentIdentifiers = diplomaticNonCurrentIdentifiers,
            unreachableDiplomaticIdentifiers = unreachableDiplomaticIdentifiers,
            specialDuplicateIdentifiers = specialDuplicateIdentifiers,
            specialMissingSources = specialMissingSources,
            invalidSpecialTypes = invalidSpecialTypes,
            specialNonCurrentIdentifiers = specialNonCurrentIdentifiers,
            unreachableSpecialIdentifiers = unreachableSpecialIdentifiers,
            authoritySeriesMissingSources = authoritySeriesMissingSources,
            authoritySeriesMissingSourceTypes = authoritySeriesMissingSourceTypes,
            authoritySeriesMissingAuthorityNames = authoritySeriesMissingAuthorityNames,
            authoritySeriesMissingRegions = authoritySeriesMissingRegions,
            invalidAuthoritySeriesTypes = invalidAuthoritySeriesTypes,
            unreachableAuthoritySeriesIdentifiers = unreachableAuthoritySeriesIdentifiers
        )
    }
}

data class PlateDatasetReport(
    val totalCount: Int,
    val countByStatus: Map<PlateStatus, Int>,
    val countByRegion: Map<String, Int>,
    val duplicateIdentifiers: Set<String>,
    val unresolvedDuplicateIdentifiers: Set<String>,
    val sharedSpecialIdentifiers: Set<String>,
    val sharedDiplomaticIdentifiers: Set<String>,
    val missingRequiredFields: Set<String>,
    val missingAuthorityTypes: Set<String>,
    val mismatchedAuthorityCounts: Set<String>,
    val diplomaticIdentifierCount: Int,
    val diplomaticCountryCodeCount: Int,
    val diplomaticOrganisationCodeCount: Int,
    val diplomaticMissingSources: Set<String>,
    val diplomaticDuplicateIdentifiers: Set<String>,
    val invalidDiplomaticTypes: Set<String>,
    val diplomaticNonCurrentIdentifiers: Set<String>,
    val unreachableDiplomaticIdentifiers: Set<String>,
    val specialDuplicateIdentifiers: Set<String>,
    val specialMissingSources: Set<String>,
    val invalidSpecialTypes: Set<String>,
    val specialNonCurrentIdentifiers: Set<String>,
    val unreachableSpecialIdentifiers: Set<String>,
    val authoritySeriesMissingSources: Set<String>,
    val authoritySeriesMissingSourceTypes: Set<String>,
    val authoritySeriesMissingAuthorityNames: Set<String>,
    val authoritySeriesMissingRegions: Set<String>,
    val invalidAuthoritySeriesTypes: Set<String>,
    val unreachableAuthoritySeriesIdentifiers: Set<String>
)