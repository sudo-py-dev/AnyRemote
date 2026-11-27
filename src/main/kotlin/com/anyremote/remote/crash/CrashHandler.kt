package com.anyremote.remote.crash

import android.content.Context
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess

/**
 * Catches any exception the app does not handle, on every thread (coroutines included, since
 * their unhandled failures end up here too), saves a report and opens [CrashActivity].
 *
 * The report screen runs in its own process, so a broken app state can't crash it again. If
 * Android blocks the screen from opening (the crash happened in the background, for example in
 * a widget), the saved report opens the next time the app starts.
 */
internal class CrashHandler
private constructor(
    private val context: Context,
    private val systemHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    companion object {
        fun install(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(
                CrashHandler(context.applicationContext, Thread.getDefaultUncaughtExceptionHandler())
            )
        }
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        try {
            CrashReport.save(context, CrashReport.format(thread, error))
            context.startActivity(CrashActivity.intent(context).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        } catch (_: Throwable) {
            // Reporting failed too; Android's handler below still records the crash.
        }
        // Android's handler records the crash and ends this process. The crash screen runs in
        // its own process and task, so it stays open.
        if (systemHandler != null) {
            systemHandler.uncaughtException(thread, error)
        } else {
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
