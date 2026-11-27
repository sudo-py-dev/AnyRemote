package com.anyremote.remote.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdatesTest {
    @Test
    fun comparesVersionComponentsNumerically() {
        assertTrue(GitHubUpdates.isNewer("0.0.10", "0.0.2"))
        assertTrue(GitHubUpdates.isNewer("1.0.0", "0.99.99"))
        assertFalse(GitHubUpdates.isNewer("0.0.2", "0.0.10"))
        assertFalse(GitHubUpdates.isNewer("0.0.2", "0.0.2"))
    }

    @Test
    fun rejectsMalformedVersions() {
        assertFalse(GitHubUpdates.isNewer("0.0.3-rc.1", "0.0.2"))
        assertFalse(GitHubUpdates.isNewer("../0.0.3", "0.0.2"))
        assertFalse(GitHubUpdates.isNewer("0.0.300000000000", "0.0.2"))
    }
}
