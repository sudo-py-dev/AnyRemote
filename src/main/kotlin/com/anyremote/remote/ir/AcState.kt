package com.anyremote.remote.ir

import androidx.compose.runtime.saveable.listSaver

/** Full Gree AC state; every key press sends the whole state, so it lives in one value. */
data class AcState(
    val power: Boolean = true,
    val temp: Int = 24,
    val mode: String = "MC",
    val speed: String = "SA",
    val swing: Boolean = true,
    /** Fixed vane position 1..5, or 0 when none was chosen. Ignored while [swing] is on. */
    val swingPosition: Int = 0,
    val turbo: Boolean = false,
    val light: Boolean = true,
    val auxHeat: Boolean = false,
    val health: Boolean = false,
    val ventilation: Boolean = false,
    val eco: Boolean = false,
    val swingH: Boolean = false,
    val mute: Boolean = false,
    val tempDisplay: Boolean = false,
) {
    fun toPulses(): IntArray =
        IREncodingEngine.compileGreeAc(
            power = power,
            temp = temp,
            mode = mode,
            fanSpeed = speed,
            swing = swing,
            swingPosition = swingPosition,
            turbo = turbo,
            light = light,
            auxHeat = auxHeat,
            health = health,
            ventilation = ventilation,
            eco = eco,
            swingH = swingH,
            mute = mute,
            tempDisplay = tempDisplay,
        )

    companion object {
        val Saver =
            listSaver<AcState, Any>(
                save = {
                    listOf(
                        it.power, it.temp, it.mode, it.speed, it.swing, it.swingPosition, it.turbo,
                        it.light, it.auxHeat, it.health, it.ventilation, it.eco, it.swingH, it.mute,
                        it.tempDisplay)
                },
                restore = {
                    AcState(
                        power = it[0] as Boolean,
                        temp = it[1] as Int,
                        mode = it[2] as String,
                        speed = it[3] as String,
                        swing = it[4] as Boolean,
                        swingPosition = it[5] as Int,
                        turbo = it[6] as Boolean,
                        light = it[7] as Boolean,
                        auxHeat = it[8] as Boolean,
                        health = it[9] as Boolean,
                        ventilation = it[10] as Boolean,
                        eco = it[11] as Boolean,
                        swingH = it[12] as Boolean,
                        mute = it[13] as Boolean,
                        tempDisplay = it[14] as Boolean,
                    )
                },
            )
    }
}
