package com.anyremote.remote.data

import android.content.Context
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

    @OptIn(ExperimentalSerializationApi::class)
    fun init(context: Context) {
        if (deviceTypes.isNotEmpty()) return
        try {
            val bytes = context.assets.open("remote.pb").use { stream -> stream.readBytes() }
            val container = ProtoBuf.decodeFromByteArray(DatabaseContainer.serializer(), bytes)
            deviceTypes = container.devices
            acOptions = container.acOptions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
