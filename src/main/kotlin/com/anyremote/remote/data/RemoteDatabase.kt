package com.anyremote.remote.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object RemoteDatabase {
    data class Brand(
        val id: Int,
        val nameEn: String,
        val category: String,
    )

    data class DeviceType(
        val id: Int,
        val nameEn: String,
        val brands: List<Brand>,
    )

    var deviceTypes: List<DeviceType> = emptyList()
        private set

    var acOptions: Map<String, List<String>> = emptyMap()
        private set

    fun hasAcOption(key: String): Boolean {
        if (acOptions.isEmpty()) return true
        val normalized = key.lowercase().replace("_", "")
        return acOptions.keys.any { it.lowercase().replace("_", "") == normalized }
    }

    /** True when remote.json couldn't be read; the dashboard shows an error instead of an empty list. */
    var loadFailed: Boolean = false
        private set

    private var loaded = false

    /** Screenshot previews only: use [devices] instead of reading remote.json from assets. */
    internal fun loadForPreview(devices: List<DeviceType>) {
        deviceTypes = devices
        acOptions = emptyMap()
        loadFailed = false
        loaded = true
    }

    fun init(context: Context) {
        if (loaded) return
        loaded = true
        try {
            val json = JSONObject(context.assets.open("remote.json").bufferedReader().use { it.readText() })
            val devices = json.getJSONArray("devices").readList { index ->
                val device = getJSONObject(index)
                val brands = device.getJSONArray("brands").readList { brandIndex ->
                    val brand = getJSONObject(brandIndex)
                    Brand(brand.getInt("id"), brand.getString("nameEn"), brand.getString("category"))
                }
                RemoteProfile.addTo(DeviceType(device.getInt("id"), device.getString("nameEn"), brands))
            }
            val options = json.optJSONObject("acOptions")
            acOptions = options?.keys()?.asSequence()?.associateWith { key ->
                options.getJSONArray(key).readList { getString(it) }
            }.orEmpty()
            deviceTypes = devices
            loadFailed = false
        } catch (e: Exception) {
            Log.e("RemoteDatabase", "Failed to load remote.json", e)
            loadFailed = true
        }
    }
}

private fun <T> JSONArray.readList(read: JSONArray.(Int) -> T): List<T> =
    List(length()) { read(it) }
