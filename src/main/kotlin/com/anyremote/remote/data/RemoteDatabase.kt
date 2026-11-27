package com.anyremote.remote.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf

object RemoteDatabase {
    @Serializable
    data class Brand(
        val id: Int,
        val nameEn: String,
        val category: String,
    )

    @Serializable
    data class DeviceType(
        val id: Int,
        val nameEn: String,
        val brands: List<Brand>,
    )

    @Serializable
    data class DatabaseContainer(
        val devices: List<DeviceType>,
        val acOptions: Map<String, List<String>> = emptyMap(),
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

    /** True when remote.pb couldn't be read; the dashboard shows an error instead of an empty list. */
    var loadFailed: Boolean = false
        private set

    private var loaded = false

    /** Screenshot previews only: use [devices] instead of reading remote.pb from assets. */
    internal fun loadForPreview(devices: List<DeviceType>) {
        deviceTypes = devices
        acOptions = emptyMap()
        loadFailed = false
        loaded = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun init(context: Context) {
        if (loaded) return
        loaded = true
        try {
            val bytes = context.assets.open("remote.pb").use { it.readBytes() }
            val container = ProtoBuf.decodeFromByteArray(DatabaseContainer.serializer(), bytes)
            deviceTypes = container.devices.map(RemoteProfile::addTo)
            acOptions = container.acOptions
            loadFailed = false
        } catch (e: Exception) {
            Log.e("RemoteDatabase", "Failed to load remote.pb", e)
            loadFailed = true
        }
    }
}
