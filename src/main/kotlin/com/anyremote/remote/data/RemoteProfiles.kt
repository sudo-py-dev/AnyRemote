package com.anyremote.remote.data

import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.ir.AcProtocol
import java.text.Normalizer

/** IDs are persisted in favorites, shortcuts and saved code sets. */
internal enum class RemoteProfile(
    val device: Int,
    val brandId: Int,
    val title: String,
    vararg val aliases: String,
) {
    TADIRAN(3, 100001, "Tadiran · Gree (wall-mounted)", "תדיראן", "תדיראן עילי", "מזגן עילי", "wall mounted", "גרי"),
    ELECTRA(3, 100002, "Electra Classic INV 17", "אלקטרה", "אלקטרה קלאסיק", "AXW12DCS", "YKR-M/003E"),
    AUX(3, 100003, "AUX KFR-35GW/BpNFW=3", "YKR-T/011", "אוקס"),
    TCL(1, 100004, "TCL 32S327", "Roku TV", "טי סי אל", "רוקו"),
    LG(1, 100005, "LG TV · NEC", "אל ג׳י", "LG infrared"),
    AMCOR(3, 100007, "Amcor ADR-853H", "אמקור", "TAC-444", "TAC-495"),
    GREE(3, 100008, "Gree", "גרי", "גריי", "Gree Electric"),
    HUMAX(2, 100009, "Humax", "יומקס", "הומקס"),
    OCTAGON(2, 100010, "Octagon", "אוקטגון"),
    ONN(12, 100006, "onn. 4K Pro", "Google TV", "גוגל טיוי");

    val acProtocol: AcProtocol get() = when (this) {
        ELECTRA, AUX -> AcProtocol.ELECTRA
        AMCOR -> AcProtocol.AMCOR
        else -> AcProtocol.GREE
    }

    companion object {
        fun find(device: Int, brand: Int) = entries.firstOrNull { it.device == device && it.brandId == brand }

        fun addTo(device: DeviceType) = device.copy(brands = device.brands + entries.filter { it.device == device.id }
            .map { Brand(it.brandId, it.title, "popular") })
    }
}

private val accents = Regex("\\p{M}+")
private val separators = Regex("[^\\p{L}\\p{N}]+")
private fun normalized(text: String) =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(accents, "").replace(separators, "")

internal fun DeviceType.searchBrands(input: String): List<Brand> {
    val terms = input.trim().split(Regex("\\s+")).map(::normalized).filter { it.isNotEmpty() }
    if (input.isBlank()) return brands.sortedBy { it.nameEn.lowercase() }
    if (terms.isEmpty()) return emptyList()
    return brands.mapNotNull { brand ->
        val name = normalized(brand.nameEn)
        val profile = RemoteProfile.find(id, brand.id)
        val aliases = profile?.aliases.orEmpty().map(::normalized)
        if (!terms.all { term -> name.contains(term) || aliases.any { it.contains(term) } }) return@mapNotNull null
        val rank = when {
            profile != null && name == terms.joinToString("") -> 0
            profile != null -> 1
            name == terms.joinToString("") -> 2
            name.startsWith(terms.first()) -> 3
            else -> 4
        }
        brand to rank
    }.sortedWith(compareBy<Pair<Brand, Int>> { it.second }.thenByDescending { it.first.category == "popular" }
        .thenBy { it.first.nameEn.lowercase() }).map { it.first }
}
