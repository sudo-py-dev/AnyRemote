package com.anyremote.remote.data

import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import java.text.Normalizer
import com.anyremote.remote.util.limitCodePoints

private val accents = Regex("\\p{M}+")
private val separators = Regex("[^\\p{L}\\p{N}]+")
private fun normalized(text: String) =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(accents, "").replace(separators, "")

internal const val MAX_BRAND_QUERY_LENGTH = 80

internal fun DeviceType.searchBrands(input: String): List<Brand> {
    val query = input.limitCodePoints(MAX_BRAND_QUERY_LENGTH).trim()
    val terms = query.split(Regex("\\s+")).map(::normalized).filter { it.isNotEmpty() }
    if (query.isEmpty()) return brands.sortedBy { it.nameEn.lowercase() }
    if (terms.isEmpty()) return emptyList()
    val exactName = terms.joinToString("")
    return brands.mapNotNull { brand ->
        val name = normalized(brand.nameEn)
        val profile = brand.profile
        val aliases = brand.aliases.map(::normalized)
        if (!terms.all { term -> name.contains(term) || aliases.any { it.contains(term) } }) return@mapNotNull null
        val rank = when {
            profile != null && name == exactName -> 0
            profile != null -> 1
            name == exactName -> 2
            name.startsWith(terms.first()) -> 3
            else -> 4
        }
        brand to rank
    }.sortedWith(compareBy<Pair<Brand, Int>> { it.second }.thenByDescending { it.first.category == "popular" }
        .thenBy { it.first.nameEn.lowercase() }).map { it.first }
}
