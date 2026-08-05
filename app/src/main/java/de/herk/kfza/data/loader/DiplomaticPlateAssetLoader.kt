package de.herk.kfza.data.loader

import android.content.Context
import de.herk.kfza.data.model.PlateEntry
import de.herk.kfza.data.model.PlateType
import java.io.InputStream

class DiplomaticPlateAssetLoader(private val context: Context) {
    fun load(): List<PlateEntry> = context.assets.open(ASSET_PATH).use(::parse)

    companion object {
        const val ASSET_PATH = "data/diplomatic_identifiers.json"

        fun parse(input: InputStream): List<PlateEntry> =
            PlateAssetJsonReaderFactory.parse(input, PlateType.DIPLOMATIC_REGIONAL_SERIES)
    }
}
