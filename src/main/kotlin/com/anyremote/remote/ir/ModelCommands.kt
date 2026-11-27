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
        // Captured commands from Flipper-IRDB Cable_Boxes/Humax and Cable_Boxes/Octagon.
        private val humax = ModelCommands(0x00, 0x10, mapOf(POWER to 0x00,
            VOLUME_UP to 0x1f, VOLUME_DOWN to 0x40, CHANNEL_UP to 0x10, CHANNEL_DOWN to 0x0f))
        private val octagon = ModelCommands(0x01, 0x04, mapOf(POWER to 0x0a, MUTE to 0x0c))
        // LIRC apex/DT250 capture uses MSB-first values. NEC sends each byte LSB first.
        private val apex = ModelCommands(0x0c, 0xfb, mapOf(
            POWER to 0x15, MENU to 0x70, BACK to 0x0f, UP to 0x02, DOWN to 0x03, OK to 0x18,
            DIGIT_0 to 0x04, DIGIT_1 to 0x05, DIGIT_2 to 0x06, DIGIT_3 to 0x07, DIGIT_4 to 0x08,
            DIGIT_5 to 0x09, DIGIT_6 to 0x0a, DIGIT_7 to 0x0b, DIGIT_8 to 0x0c, DIGIT_9 to 0x0d,
            DASH to 0x36,
        ))
        // Flipper-IRDB DVB-T captures.
        private val tilview = ModelCommands(0x35, 0xca, mapOf(
            POWER to 0x01, MENU to 0x5b, BACK to 0x16, MUTE to 0x12,
            VOLUME_DOWN to 0x00, VOLUME_UP to 0x07, CHANNEL_DOWN to 0x04, CHANNEL_UP to 0x0b,
            OK to 0x15, UP to 0x17, RIGHT to 0x41, DOWN to 0x1a, LEFT to 0x42,
        ))
        private val tesla = ModelCommands(0x01, 0xfd, mapOf(
            POWER to 0xdc, VOLUME_UP to 0xc1, VOLUME_DOWN to 0x99, MUTE to 0x9c,
        ))
        // Published converter captures are linked in README.
        private val storex = ModelCommands(0x04, 0xfb, mapOf(
            POWER to 0x1a, VOLUME_UP to 0x45, VOLUME_DOWN to 0x19, MUTE to 0x0e,
            OK to 0x5c, CHANNEL_UP to 0x44, CHANNEL_DOWN to 0x1d, BACK to 0x1f,
            HOME to 0x0a, LEFT to 0x1c, RIGHT to 0x48, DIGIT_0 to 0x47,
            DIGIT_1 to 0x13, DIGIT_2 to 0x10, DIGIT_3 to 0x11, DIGIT_4 to 0x0f,
            DIGIT_5 to 0x0c, DIGIT_6 to 0x0d, DIGIT_7 to 0x0b, DIGIT_8 to 0x08,
            DIGIT_9 to 0x09,
        ))
        private val diprogress = ModelCommands(0x01, 0xfe, mapOf(
            POWER to 0x19, CHANNEL_UP to 0x1e, CHANNEL_DOWN to 0x1d, VOLUME_UP to 0x18,
            VOLUME_DOWN to 0x09, UP to 0x06, DOWN to 0x16, LEFT to 0x5a,
            RIGHT to 0x1b, OK to 0x1a, MENU to 0x55, BACK to 0x0b,
            MUTE to 0x12, DIGIT_0 to 0x0f, DIGIT_1 to 0x52, DIGIT_2 to 0x50,
            DIGIT_3 to 0x10, DIGIT_4 to 0x56, DIGIT_5 to 0x54, DIGIT_6 to 0x14,
            DIGIT_7 to 0x4e, DIGIT_8 to 0x4c, DIGIT_9 to 0x0c,
        ))
        private val octagonSf8008 = ModelCommands(0x07, 0xf8, mapOf(
            POWER to 0x59, MUTE to 0x19, UP to 0x06, DOWN to 0x16,
            LEFT to 0x5a, RIGHT to 0x1b, OK to 0x1a, MENU to 0x45,
            BACK to 0x05, VOLUME_UP to 0x66, VOLUME_DOWN to 0x67, CHANNEL_UP to 0x13,
            CHANNEL_DOWN to 0x17, DIGIT_0 to 0x0f, DIGIT_1 to 0x52, DIGIT_2 to 0x50,
            DIGIT_3 to 0x10, DIGIT_4 to 0x56, DIGIT_5 to 0x54, DIGIT_6 to 0x14,
            DIGIT_7 to 0x4e, DIGIT_8 to 0x4c, DIGIT_9 to 0x0c,
        ))
        private val thomson = ModelCommands(0x00, 0xbf, mapOf(
            POWER to 0x59, MENU to 0x45, BACK to 0x05, UP to 0x06,
            DOWN to 0x16, LEFT to 0x5a, RIGHT to 0x1b, OK to 0x1a,
            VOLUME_DOWN to 0x11, VOLUME_UP to 0x0d, MUTE to 0x19, CHANNEL_DOWN to 0x16,
            CHANNEL_UP to 0x06, DIGIT_0 to 0x0f, DIGIT_1 to 0x52, DIGIT_2 to 0x50,
            DIGIT_3 to 0x10, DIGIT_4 to 0x56, DIGIT_5 to 0x54, DIGIT_6 to 0x14,
            DIGIT_7 to 0x4e, DIGIT_8 to 0x4c, DIGIT_9 to 0x0c,
        ))

        fun forProfile(profile: RemoteProfile?) = when (profile) {
            RemoteProfile.TCL -> tcl
            RemoteProfile.LG -> lg
            RemoteProfile.ONN -> onn
            RemoteProfile.APEX_DT250 -> apex
            RemoteProfile.TILVIEW -> tilview
            RemoteProfile.TESLA_TE300 -> tesla
            RemoteProfile.STOREX -> storex
            RemoteProfile.DIPROGRESS -> diprogress
            RemoteProfile.OCTAGON_SF8008 -> octagonSf8008
            RemoteProfile.THOMSON_THT501 -> thomson
            RemoteProfile.HUMAX -> humax
            RemoteProfile.OCTAGON -> octagon
            else -> null
        }
    }
}
