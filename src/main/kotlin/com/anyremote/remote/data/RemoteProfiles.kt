package com.anyremote.remote.data

import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.util.limitCodePoints
import java.text.Normalizer

private val accents = Regex("\\p{M}+")
private val separators = Regex("[^\\p{L}\\p{N}]+")
private val whitespace = Regex("\\s+")

private fun normalized(text: String) =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(accents, "")
        .replace(separators, "")

internal const val MAX_BRAND_QUERY_LENGTH = 80

internal class BrandSearchIndex(device: DeviceType) {
    private data class Entry(val brand: Brand, val name: String, val aliases: List<String>)

    val alphabetical = device.brands.sortedBy { it.nameEn.lowercase() }
    private val entries = alphabetical.map { brand ->
        Entry(brand, normalized(brand.nameEn), brand.aliases.map(::normalized))
    }

    fun search(input: String): List<Brand> {
        val query = input.limitCodePoints(MAX_BRAND_QUERY_LENGTH).trim()
        if (query.isEmpty()) return alphabetical
        val terms = query.split(whitespace).map(::normalized).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return emptyList()
        val exactName = terms.joinToString("")
        return entries
            .mapNotNull { (brand, name, aliases) ->
                if (
                    !terms.all { term -> name.contains(term) || aliases.any { it.contains(term) } }
                ) {
                    return@mapNotNull null
                }
                val rank =
                    when {
                        brand.profile != null && name == exactName -> 0
                        brand.profile != null -> 1
                        name == exactName -> 2
                        name.startsWith(terms.first()) -> 3
                        else -> 4
                    }
                brand to rank
            }
            .sortedWith(
                compareBy<Pair<Brand, Int>> { it.second }
                    .thenByDescending { it.first.category == "popular" }
            )
            .map { it.first }
    }
}
