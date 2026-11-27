package com.anyremote.remote.ir

/** Logical remote buttons. Every key has its own NEC command, so no two buttons share a code. */
enum class RemoteKey(val command: Int) {
    DIGIT_0(0x00),
    DIGIT_1(0x01),
    DIGIT_2(0x02),
    DIGIT_3(0x03),
    DIGIT_4(0x04),
    DIGIT_5(0x05),
    DIGIT_6(0x06),
    DIGIT_7(0x07),
    DIGIT_8(0x08),
    DIGIT_9(0x09),
    DASH(0x0A),
    BACKSPACE(0x0B),
    POWER(0x10),
    SOURCE(0x11),
    MUTE(0x12),
    OK(0x13),
    UP(0x14),
    DOWN(0x15),
    LEFT(0x16),
    RIGHT(0x17),
    BACK(0x18),
    HOME(0x19),
    MENU(0x1A),
    VOLUME_UP(0x1B),
    VOLUME_DOWN(0x1C),
    CHANNEL_UP(0x1D),
    CHANNEL_DOWN(0x1E),
    SPEED_UP(0x20),
    SPEED_DOWN(0x21),
    OSCILLATE(0x22),
    MODE(0x23),
    TIMER(0x24),
    FAN_SPEED(0x25),
    SOURCE_BLUETOOTH(0x28),
    SOURCE_HDMI(0x29),
    SOURCE_OPTICAL(0x2A),
    SOURCE_AUX(0x2B),
    FOCUS_DOWN(0x30),
    FOCUS_UP(0x31),
    SHUTTER(0x38),
    SHUTTER_2S(0x39),
    SHUTTER_5S(0x3A),
    SHUTTER_10S(0x3B),
    PLAY(0x40),
    PAUSE(0x41),
    STOP(0x42),
    PREVIOUS(0x43),
    NEXT(0x44);

    companion object {
        fun digit(n: Int): RemoteKey {
            require(n in 0..9) { "Not a digit: $n" }
            return entries[DIGIT_0.ordinal + n]
        }
    }
}
