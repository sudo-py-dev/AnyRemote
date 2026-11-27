package com.anyremote.remote.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteNameTest {
    @Test
    fun rejectsBlankAndOverLimitNames() {
        assertFalse(RemoteName.isValid("   "))
        assertFalse(RemoteName.isValid("W".repeat(41)))
    }

    @Test
    fun acceptsMaximumLengthAndIgnoresOuterWhitespace() {
        assertTrue(RemoteName.isValid("  ${"א".repeat(40)}  "))
        assertTrue(RemoteName.isValid("📺".repeat(40)))
        assertFalse(RemoteName.isValid("📺".repeat(41)))
    }
}
