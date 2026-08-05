package de.herk.kfza.data.model

enum class PlateStatus {
    ACTIVE,
    REINTRODUCED,
    EXPIRING,
    HISTORICAL,
    CONVENTIONAL
}

enum class PlateType {
    GEOGRAPHICAL,
    FEDERAL_ORGAN,
    STATE_ORGAN,
    FEDERAL_SUBJECT_AREA,
    AUTHORITY,
    FINANCE_ADMINISTRATION,
    CONSTITUTIONAL_COURT,
    AUTHORITY_SERIES,
    DEFENCE,
    RELIEF_ORGANISATION,
    GOVERNMENT,
    CONSTITUTIONAL_BODY,
    DIPLOMATIC_CORPS,
    INTERNATIONAL_ORGANISATION,
    DIPLOMATIC,
    DIPLOMATIC_REGIONAL_SERIES,
    CONSULAR_REGIONAL_SERIES,
    DIPLOMATIC_VEHICLE_MARK,
    CONSULAR_VEHICLE_MARK
}

enum class SourceType {
    PRIMARY,
    SECONDARY
}

enum class GeographicalAuthorityType {
    DISTRICT_AUTHORITY,
    STATE_POLICE_DIRECTORATE,
    MAGISTRATE,
    EXPOSITURE
}

data class GeographicalAuthority(
    val name: String,
    val authorityType: GeographicalAuthorityType
)

data class PlateEntry(
    val identifier: String,
    val authorityNames: List<String>,
    val region: String,
    val status: PlateStatus,
    val type: PlateType = PlateType.GEOGRAPHICAL,
    val authorities: List<GeographicalAuthority> = emptyList(),
    val source: String? = null,
    val sourceType: SourceType? = null,
    val notes: String? = null,
    val regions: List<String> = listOf(region),
    val types: List<PlateType> = listOf(type)
)