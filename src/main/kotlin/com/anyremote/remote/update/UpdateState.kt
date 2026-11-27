package com.anyremote.remote.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anyremote.remote.BuildConfig
import kotlinx.coroutines.CancellationException

/** Update check shared by the launch check, the update dialog and Settings. */
internal object UpdateState {
    enum class Status {
        Idle,
        Checking,
        UpToDate,
        Available,
        Failed,
    }

    var status by mutableStateOf(Status.Idle)
        private set

    var release by mutableStateOf<GitHubUpdates.Release?>(null)
        private set

    /** Whether the update dialog is on screen. */
    var promptVisible by mutableStateOf(false)
        private set

    /** Debug builds are signed with another key, so they can't install releases. */
    val canInstall: Boolean = !BuildConfig.DEBUG

    /** Opens the update dialog, in builds that can install the update. */
    fun showPrompt() {
        if (canInstall) promptVisible = true
    }

    fun hidePrompt() {
        promptVisible = false
    }

    val installedVersion: String = BuildConfig.VERSION_NAME

    /** Checks GitHub for a newer release and opens the dialog when one exists. */
    suspend fun check() {
        if (status == Status.Checking) return
        val previous = status
        status = Status.Checking
        try {
            val latest = GitHubUpdates.latest(installedVersion)
            release = latest
            status = if (latest != null) Status.Available else Status.UpToDate
            if (latest != null) showPrompt()
        } catch (error: CancellationException) {
            status = previous
            throw error
        } catch (_: Exception) {
            status = Status.Failed
        }
    }
}
