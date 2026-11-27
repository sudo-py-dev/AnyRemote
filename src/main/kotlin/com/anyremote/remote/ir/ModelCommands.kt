package com.anyremote.remote.ir

/** NEC address bytes and logical button commands loaded from the catalog. */
data class ModelCommands(val address: Int, val secondAddress: Int, val keys: Map<RemoteKey, Int>) {
    fun pulses(key: RemoteKey) =
        keys[key]?.let { IREncodingEngine.compileGenericNec(address, it, secondAddress) } ?: intArrayOf()
}
