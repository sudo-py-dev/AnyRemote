package com.anyremote.remote.ir

import com.anyremote.remote.data.RemoteProfile
import com.anyremote.remote.ir.RemoteKey.*

/** Decoded NEC address bytes and command bytes from the Flipper-IRDB captures linked in README. */
internal data class ModelCommands(val address: Int, val secondAddress: Int, val keys: Map<RemoteKey, Int>) {
    fun pulses(key: RemoteKey) = keys[key]?.let { IREncodingEngine.compileGenericNec(address, it, secondAddress) } ?: intArrayOf()

    companion object {
        private val tcl = ModelCommands(0xea, 0xc7, mapOf(POWER to 0x17, VOLUME_UP to 0x0f, VOLUME_DOWN to 0x10,
            OK to 0x2a, UP to 0x19, DOWN to 0x33, RIGHT to 0x2d, LEFT to 0x1e, HOME to 0x03, BACK to 0x66))
        private val lg = ModelCommands(0x04, 0xfb, mapOf(POWER to 0x08, MUTE to 0x09, VOLUME_UP to 0x02,
            VOLUME_DOWN to 0x03, CHANNEL_UP to 0x00, CHANNEL_DOWN to 0x01))
        private val onn = ModelCommands(0x88, 0x77, mapOf(HOME to 0x47, BACK to 0x48, OK to 0x19, UP to 0x15,
            DOWN to 0x16, LEFT to 0x17, RIGHT to 0x18, CHANNEL_UP to 0x33, CHANNEL_DOWN to 0x34))
        fun forProfile(profile: RemoteProfile?) = when (profile) {
            RemoteProfile.TCL -> tcl
            RemoteProfile.LG -> lg
            RemoteProfile.ONN -> onn
            else -> null
        }
    }
}
