package com.anyremote.remote.ir

import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.data.RemoteProfile
import com.anyremote.remote.data.searchBrands
import org.junit.Assert.*
import org.junit.Test

class RemoteProfileTest {
    @Test fun modelAndHebrewQueriesFindTheRightProfiles() {
        val devices = RemoteProfile.addTo(DeviceType(3, "AC", emptyList()))
        for ((query, profile) in mapOf("תַּדִירָאן" to RemoteProfile.TADIRAN, "אלקטרה 17" to RemoteProfile.ELECTRA,
            "003e ykr" to RemoteProfile.ELECTRA, "tac 495" to RemoteProfile.AMCOR, "גרי" to RemoteProfile.GREE)) {
            assertTrue(query, devices.searchBrands(query).any { it.id == profile.brandId })
        }
        assertEquals(setOf(RemoteProfile.GREE.brandId, RemoteProfile.TADIRAN.brandId),
            devices.searchBrands("Gree").map { it.id }.toSet())
        assertTrue(devices.searchBrands("32S327").isEmpty())
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
        val commands = ModelCommands.forProfile(RemoteProfile.TCL)!!
        val power = commands.pulses(RemoteKey.POWER)
        assertEquals(listOf(0xea, 0xc7, 0x17, 0xe8), bytes(power, 4) { power[it + 1] == 1690 })
        assertTrue(commands.pulses(RemoteKey.DIGIT_0).isEmpty())
    }
}
