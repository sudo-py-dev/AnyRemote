package com.anyremote.remote.data

import android.content.Context
import android.util.Log
import com.anyremote.remote.ir.AcProtocol
import com.anyremote.remote.ir.ModelCommands
import com.anyremote.remote.ir.RemoteKey
import org.json.JSONArray
import org.json.JSONObject

object RemoteDatabase {
    sealed interface RemoteProfile {
        data class AirConditioner(val protocol: AcProtocol) : RemoteProfile
        data class Nec(val commands: ModelCommands, val testKey: RemoteKey) : RemoteProfile
    }

    data class Brand(
        val id: Int,
        val nameEn: String,
        val category: String,
        val aliases: List<String> = emptyList(),
        val profile: RemoteProfile? = null,
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

    private fun JSONObject.readProfile(): RemoteProfile {
        val protocol = getString("protocol")
        if (protocol != "NEC") return RemoteProfile.AirConditioner(AcProtocol.valueOf(protocol))

        val codes = getJSONObject("keys")
        val keys = codes.keys().asSequence().associate { key -> RemoteKey.valueOf(key) to codes.readByte(key) }
        val testKey = RemoteKey.valueOf(optString("testKey", RemoteKey.POWER.name))
        require(testKey in keys) { "Missing test command" }
        return RemoteProfile.Nec(
            ModelCommands(readByte("address"), readByte("secondAddress"), keys),
            testKey,
        )
    }

    private fun JSONObject.readByte(key: String): Int =
        getInt(key).also { require(it in 0..255) { "Invalid IR byte for $key" } }

    fun init(context: Context) {
        if (loaded) return
        loaded = true
        try {
            val json = JSONObject(context.assets.open("remote.json").bufferedReader().use { it.readText() })
            val devices = json.getJSONArray("devices").readList { index ->
                val device = getJSONObject(index)
                val brands = device.getJSONArray("brands").readList { brandIndex ->
                    val brand = getJSONObject(brandIndex)
                    Brand(
                        id = brand.getInt("id"),
                        nameEn = brand.getString("nameEn"),
                        category = brand.getString("category"),
                        aliases = brand.optJSONArray("aliases")?.readList { getString(it) }.orEmpty(),
                        profile = brand.optJSONObject("ir")?.readProfile(),
                    )
                }
                require(brands.distinctBy { it.id }.size == brands.size) { "Duplicate brand ID" }
                DeviceType(device.getInt("id"), device.getString("nameEn"), brands)
            }
            require(devices.distinctBy { it.id }.size == devices.size) { "Duplicate device ID" }
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
