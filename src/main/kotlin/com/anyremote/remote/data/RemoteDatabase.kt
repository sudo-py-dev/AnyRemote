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

    var acOptions: Map<String, List<String>> = emptyMap()
        private set

    fun hasAcOption(key: String): Boolean {
        if (acOptions.isEmpty()) return true
        val normalized = key.lowercase().replace("_", "")
        return acOptions.keys.any { it.lowercase().replace("_", "") == normalized }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun init(context: Context) {
        loadAcOptions(context)
        if (deviceTypes.isNotEmpty()) return
        try {

            val bytes = context.assets.open("remote.pb").use { stream -> stream.readBytes() }
            val container = ProtoBuf.decodeFromByteArray(DatabaseContainer.serializer(), bytes)
            deviceTypes = container.devices
        } catch (e: Exception) {
            e.printStackTrace()

            initFromJson(context)
        }
    }

    private fun loadAcOptions(context: Context) {
        if (acOptions.isNotEmpty()) return
        try {
            val jsonString =
                context.assets.open("remotes_database.json").use { stream ->
                    stream.bufferedReader().use { it.readText() }
                }
            val root = JSONObject(jsonString)
            if (root.has("gree_ac_protocol")) {
                val protocol = root.getJSONObject("gree_ac_protocol")
                if (protocol.has("options")) {
                    val optionsObj = protocol.getJSONObject("options")
                    val map = mutableMapOf<String, List<String>>()
                    val keys = optionsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val arr = optionsObj.getJSONArray(key)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getString(i))
                        }
                        map[key] = list
                    }
                    acOptions = map
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
