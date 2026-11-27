package com.anyremote.remote.ir

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IrEncodingTest {
    private fun ByteArray.hex() = joinToString(" ") { "%02X".format(it) }

    @Test
    fun defaultAcStateBytes() {
        // Power on, 24 °C, cool, auto fan, auto swing, light on.
        assertEquals("49 08 20 50 01 21 00 D0", AcState().toBytes().hex())
    }

    @Test
    fun checksumMatchesHighNibbleOfLastByte() {
        val states =
            listOf(
                AcState(),
                AcState(power = false),
                AcState(temp = 30, mode = "MH", speed = "SH", swing = false, swingPosition = 3),
                AcState(turbo = true, eco = true, swingH = true, tempDisplay = true),
            )
        for (s in states) {
            val bytes = s.toBytes()
            assertEquals(
                s.toString(),
                IREncodingEngine.greeChecksum(bytes),
                (bytes[7].toInt() and 0xFF) shr 4,
            )
        }
    }

    /** Each toggle must flip only its own bits (plus the checksum), never another field's. */
    @Test
    fun togglesDoNotOverlap() {
        val base = AcState(light = false, swing = false).toBytes()
        val expected =
            mapOf(
                AcState(light = false, swing = false, turbo = true) to (2 to 0x10),
                AcState(light = true, swing = false) to (2 to 0x20),
                AcState(light = false, swing = false, swingH = true) to (4 to 0x10),
                AcState(light = false, swing = false, tempDisplay = true) to (5 to 0x03),
                AcState(light = false, swing = false, eco = true) to (7 to 0x04),
            )
        for ((state, change) in expected) {
            val bytes = state.toBytes()
            for (i in 0..6) {
                val diff = (bytes[i].toInt() xor base[i].toInt()) and 0xFF
                val allowed = if (i == change.first) change.second else 0
                assertEquals("$state byte $i", 0, diff and allowed.inv())
                if (i == change.first) assertNotEquals("$state byte $i unchanged", 0, diff)
            }
            if (change.first == 7) {
                assertEquals(
                    "$state byte 7",
                    change.second,
                    (bytes[7].toInt() xor base[7].toInt()) and 0x0F,
                )
            }
        }
    }

    @Test
    fun vanePositionsUseSwingVNibble() {
        for (pos in 1..5) {
            val b4 = AcState(swing = false, swingPosition = pos).toBytes()[4].toInt()
            assertEquals(pos + 1, b4 and 0x0F)
        }
        assertEquals(1, AcState(swing = true, swingPosition = 4).toBytes()[4].toInt() and 0x0F)
    }

    @Test
    fun powerOffClearsPoweredFeatures() {
        val off =
            AcState(power = false, turbo = true, eco = true, swingH = true, tempDisplay = true)
                .toBytes()
        assertEquals(0, off[2].toInt() and 0x30)
        assertEquals(0, off[4].toInt() and 0x70)
        assertEquals(0x21, off[5].toInt())
        assertEquals(0, off[7].toInt() and 0x04)
    }

    @Test
    fun greeFrameLength() {
        // Header (2) + 32 bits + 3 footer bits + gap (2) + 32 bits + gap (2), two entries per bit.
        assertEquals(2 + 64 + 6 + 2 + 64 + 2, AcState().toPulses().size)
    }

    @Test
    fun necFrameCarriesInvertedAddressAndCommand() {
        val pulses = IREncodingEngine.compileGenericNec(address = 0x01, command = 0x10)
        assertEquals(2 + 64 + 2, pulses.size)
        val bits = (0..31).map { if (pulses[3 + it * 2] == 1690) 1 else 0 }
        val data = bits.withIndex().sumOf { (i, b) -> b shl i }
        assertEquals(0x01, data and 0xFF)
        assertEquals(0xFE, (data shr 8) and 0xFF)
        assertEquals(0x10, (data shr 16) and 0xFF)
        assertEquals(0xEF, (data ushr 24) and 0xFF)
    }

    @Test
    fun remoteKeyCommandsAreUnique() {
        val commands = RemoteKey.entries.map { it.command }
        assertEquals(commands.size, commands.toSet().size)
        assertEquals(RemoteKey.DIGIT_5, RemoteKey.digit(5))
        assertNotEquals(RemoteKey.POWER.command, RemoteKey.digit(5).command)
    }

    @Test
    fun codesetShiftsAddressAndDefaultsToBase() {
        assertEquals(0x01, IrCodebook.necAddress(deviceId = 1, codeset = 1))
        assertEquals(0x06, IrCodebook.necAddress(deviceId = 6, codeset = 1))
        assertEquals(0x21, IrCodebook.necAddress(deviceId = 1, codeset = 3))
        assertEquals(IrCodebook.necAddress(1, 1), IrCodebook.necAddress(1, 0))
        assertArrayEquals(
            IREncodingEngine.compileGenericNec(0x01, RemoteKey.POWER.command),
            IrCodebook.pulses(1, 1, RemoteKey.POWER),
        )
    }
}
