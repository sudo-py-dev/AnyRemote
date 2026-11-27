package com.anyremote.remote.ir

import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.RemoteProfile
import com.anyremote.remote.data.BrandSearchIndex
import com.anyremote.remote.data.searchBrands
import org.junit.Assert.*
import org.junit.Test

class RemoteProfileTest {
    @Test fun modelAndHebrewQueriesFindTheRightProfiles() {
        val devices = DeviceType(3, "AC", listOf(
            Brand(1, "Tadiran · Gree", "popular", listOf("תדיראן"), RemoteProfile.AirConditioner(AcProtocol.GREE)),
            Brand(2, "Electra Classic INV 17", "popular", listOf("אלקטרה", "YKR-M/003E"),
                RemoteProfile.AirConditioner(AcProtocol.ELECTRA)),
            Brand(3, "Amcor ADR-853H", "popular", listOf("TAC-495"), RemoteProfile.AirConditioner(AcProtocol.AMCOR)),
            Brand(4, "Gree", "popular", listOf("גרי"), RemoteProfile.AirConditioner(AcProtocol.GREE)),
        ))
        for ((query, id) in mapOf("תַּדִירָאן" to 1, "אלקטרה 17" to 2, "003e ykr" to 2, "tac 495" to 3, "גרי" to 4)) {
            assertTrue(query, devices.searchBrands(query).any { it.id == id })
        }
        assertEquals(setOf(1, 4), devices.searchBrands("Gree").map { it.id }.toSet())
        assertTrue(devices.searchBrands("32S327").isEmpty())
    }

    @Test fun cachedSearchPreservesRankingAcrossQueries() {
        val index = BrandSearchIndex(DeviceType(1, "TV", listOf(
            Brand(1, "Vizio Alpha", "normal"),
            Brand(2, "Vizio Beta", "popular"),
            Brand(3, "Vizio", "normal"),
            Brand(4, "Vízio V5", "normal", listOf("V-5"),
                RemoteProfile.Nec(ModelCommands(0, 255, mapOf(RemoteKey.POWER to 1)), RemoteKey.POWER)),
        )))

        assertEquals(listOf(3, 1, 2, 4), index.search(" ").map { it.id })
        assertEquals(listOf(4, 3, 2, 1), index.search("vizio").map { it.id })
        assertEquals(listOf(4), index.search("V-5").map { it.id })
        assertTrue(index.search("…").isEmpty())
        assertEquals(listOf(4, 3, 2, 1), index.search("vizio").map { it.id })
    }

    private fun bytes(pulses: IntArray, count: Int, isOne: (Int) -> Boolean) =
        List(count) { byte ->
            (0..7).sumOf { bit -> if (isOne(2 + 16 * byte + 2 * bit)) 1 shl bit else 0 }
        }

    @Test fun electraDefaultAndAmcorRepeatedFrames() {
        val electra = AcState().toPulses(AcProtocol.ELECTRA)
        assertEquals(listOf(0xc3, 0x80, 0xe0, 0, 0xa0, 0, 0x20, 0, 0, 0x20, 0, 8, 0x0b),
            bytes(electra, 13) { electra[it + 1] == 1647 })
        val amcor = AcState().toPulses(AcProtocol.AMCOR)
        assertEquals(listOf(1, 0x41, 0x30, 0, 0, 0x30, 0, 12), bytes(amcor, 8) { amcor[it] == 1500 })
        assertArrayEquals(amcor.copyOfRange(0, 132), amcor.copyOfRange(132, 264))
    }

    @Test fun tclPowerUsesCapturedExtendedAddress() {
        val commands = ModelCommands(0xea, 0xc7, mapOf(RemoteKey.POWER to 0x17))
        val power = commands.pulses(RemoteKey.POWER)
        assertEquals(listOf(0xea, 0xc7, 0x17, 0xe8), bytes(power, 4) { power[it + 1] == 1690 })
        assertTrue(commands.pulses(RemoteKey.DIGIT_0).isEmpty())
    }
}
