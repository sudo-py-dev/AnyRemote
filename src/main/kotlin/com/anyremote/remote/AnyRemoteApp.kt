package com.anyremote.remote

import android.app.Application
import android.os.Build
import com.anyremote.remote.crash.CrashHandler
import java.io.File

class AnyRemoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The crash screen's own process must not report its own crashes, or it could loop.
        if (!processName().endsWith(":crash")) CrashHandler.install(this)
    }

    private fun processName(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            runCatching { File("/proc/self/cmdline").readText().trimEnd('\u0000') }.getOrDefault("")
        }
}
