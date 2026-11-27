package com.anyremote.remote.ir

/**
 * Single place that turns (device, codeset, key) into an IR frame.
 *
 * remote.json does not carry real per-brand IR codes yet, so the NEC address is derived from the
 * device type and the codeset the user confirmed in the match wizard. The test pulse in the wizard
 * and the live remote both go through here, so whatever the user matched is what gets sent.
 */
object IrCodebook {
    const val CARRIER_HZ = 38_000
    const val CODESET_COUNT = 5

    private val baseAddress = mapOf(6 to 0x06, 8 to 0x08, 10 to 0x0A, 13 to 0x0F)

    fun necAddress(deviceId: Int, codeset: Int): Int {
        val offset = (codeset.coerceIn(1, CODESET_COUNT) - 1) * 0x10
        return ((baseAddress[deviceId] ?: 0x01) + offset) and 0xFF
    }

    fun pulses(deviceId: Int, codeset: Int, key: RemoteKey): IntArray =
        IREncodingEngine.compileGenericNec(necAddress(deviceId, codeset), key.command)
}
