package com.anyremote.remote.data

import android.content.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import org.json.JSONObject

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
    )

    var deviceTypes: List<DeviceType> = emptyList()
        private set

    @OptIn(ExperimentalSerializationApi::class)
    fun init(context: Context) {
        if (deviceTypes.isNotEmpty()) return
        try {
            // Read from compiled protobuf binary file
            val bytes =
                context.assets.open("remotes_database.pb").use { stream -> stream.readBytes() }
            val container = ProtoBuf.decodeFromByteArray(DatabaseContainer.serializer(), bytes)
            deviceTypes = container.devices
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to JSON in case the binary is not found or fails to parse
            initFromJson(context)
        }
    }

    private fun initFromJson(context: Context) {
        try {
            val jsonString =
                context.assets.open("remotes_database.json").use { stream ->
                    stream.bufferedReader().use { it.readText() }
                }
            val root = JSONObject(jsonString)
            val devicesArray = root.getJSONArray("devices")
            val list = mutableListOf<DeviceType>()

            for (i in 0 until devicesArray.length()) {
                val devObj = devicesArray.getJSONObject(i)
                val devId = devObj.getInt("id")
                val devNameEn = devObj.getString("name_en")

                val brandsArray = devObj.getJSONArray("brands")
                val brandsList = mutableListOf<Brand>()
                for (j in 0 until brandsArray.length()) {
                    val brandObj = brandsArray.getJSONObject(j)
                    val brandId = brandObj.getInt("id")
                    val brandNameEn = brandObj.getString("name_en")
                    val brandCategory = brandObj.optString("category", "other")
                    brandsList.add(Brand(brandId, brandNameEn, brandCategory))
                }
                list.add(DeviceType(devId, devNameEn, brandsList))
            }
            deviceTypes = list
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
