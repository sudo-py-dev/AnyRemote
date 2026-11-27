package com.anyremote.remote.crash

import android.content.Context
import android.os.Build
import android.os.Looper
import com.anyremote.remote.BuildConfig
import java.io.File
import java.net.URLEncoder

/**
 * Builds, stores and shares the report for a crash the app did not handle.
 *
 * A report holds only what is needed to find the bug: the app version, Android version, phone
 * model, whether the crash was on the main thread, and the error types with their code locations.
 * Error messages can quote user data (remote names, links, file paths), so they are scrubbed and
 * shortened. Nothing else about the user or their saved remotes is included, and every part has
 * a size limit so a runaway error can't produce a huge report.
 */
internal object CrashReport {
    private const val FILE_NAME = "last-crash.txt"

    // .github/ISSUE_TEMPLATE/crash_report.yml; "report" is the id of its report field.
    private const val ISSUES_URL =
        "https://github.com/sudo-py-dev/AnyRemote/issues/new?template=crash_report.yml"

    internal const val MAX_CAUSES = 5
    internal const val MAX_FRAMES = 30
    internal const val MAX_MESSAGE_CHARS = 160
    internal const val MAX_REPORT_CHARS = 16 * 1024
    private const val MAX_TITLE_CHARS = 100

    // GitHub rejects very long issue links, so long traces are cut to fit the link.
    internal const val MAX_ISSUE_URL_CHARS = 7000
    internal const val TRUNCATED_NOTE = "… (trace shortened; the full report is on your clipboard)"

    fun format(thread: Thread, error: Throwable): String =
        format(
            appVersion =
                "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.FLAVOR}" +
                    if (BuildConfig.DEBUG) " debug" else "",
            android = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            mainThread = thread == Looper.getMainLooper().thread,
            error = error,
        )

    internal fun format(
        appVersion: String,
        android: String,
        device: String,
        mainThread: Boolean,
        error: Throwable,
    ): String {
        val header =
            listOf(
                "AnyRemote ${scrub(appVersion)}",
                "Android ${scrub(android)}",
                "Device: ${scrub(device)}",
                "Thread: ${if (mainThread) "main" else "background"}",
            )
        return (header + "" + trace(error)).joinToString("\n").take(MAX_REPORT_CHARS)
    }

    /** Error types and code locations, like a stack trace, with messages scrubbed. */
    private fun trace(error: Throwable): List<String> {
        val lines = mutableListOf<String>()
        val seen = mutableSetOf<Throwable>()
        var current: Throwable? = error
        while (current != null && seen.add(current)) {
            if (seen.size > MAX_CAUSES) {
                lines += "… more causes omitted"
                break
            }
            val message = current.message?.let(::scrub).orEmpty()
            val name = current.javaClass.name
            val heading = if (message.isEmpty()) name else "$name: $message"
            lines += if (current === error) heading else "Caused by: $heading"
            val frames = current.stackTrace
            frames.take(MAX_FRAMES).forEach { lines += "\tat $it" }
            if (frames.size > MAX_FRAMES) lines += "\t… ${frames.size - MAX_FRAMES} more"
            current = current.cause
        }
        return lines
    }

    private val scrubbers =
        listOf(
            Regex("\\b[a-zA-Z][a-zA-Z0-9+.-]*://\\S+") to "<link>",
            Regex("[\\w.%+-]+@[\\w-]+(\\.[\\w-]+)+") to "<email>",
            Regex("(?<![\\w.])/[\\w.-]+(/[\\w.-]*)+") to "<path>",
            Regex("\\b\\d{1,3}(\\.\\d{1,3}){3}\\b") to "<ip>",
            Regex("\\b[0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5}\\b") to "<mac>",
            Regex("\\d{7,}") to "<number>",
            Regex("\"[^\"]*\"") to "\"<text>\"",
            Regex("'[^']*'") to "'<text>'",
            Regex("“[^”]*”") to "“<text>”",
        )

    /**
     * Removes what could identify the user or quote their data from an error message: links,
     * email addresses, file paths, network addresses, long numbers and quoted text (which is
     * usually something the user typed, such as a remote name). Line breaks and control
     * characters are removed so a message can't add fake lines to the report.
     */
    internal fun scrub(text: String): String {
        var result = text.replace(Regex("[\\p{Cntrl}\\u2028\\u2029]+"), " ")
        for ((pattern, replacement) in scrubbers) result = pattern.replace(result, replacement)
        result = result.trim()
        return if (result.length <= MAX_MESSAGE_CHARS) result
        else result.take(MAX_MESSAGE_CHARS - 1) + "…"
    }

    /** One-line summary of the root cause, used as the issue title. */
    fun title(report: String): String {
        val cause =
            report.lineSequence().lastOrNull { it.startsWith("Caused by: ") }?.removePrefix("Caused by: ")
                ?: report.substringAfter("\n\n", "").lineSequence().firstOrNull().orEmpty()
        val name = cause.substringBefore(':').substringAfterLast('.')
        val message = cause.substringAfter(':', "").trim()
        val summary = if (message.isEmpty()) name else "$name: $message"
        return "Crash: $summary".take(MAX_TITLE_CHARS)
    }

    /** Link to a new crash issue with the report filled in, shortened to fit if needed. */
    fun issueUrl(report: String): String {
        val title = encode(title(report))
        var lines = report.lines()
        var shortened = false
        while (true) {
            val text = lines.joinToString("\n") + if (shortened) "\n$TRUNCATED_NOTE" else ""
            val url = "$ISSUES_URL&title=$title&report=${encode(text)}"
            if (url.length <= MAX_ISSUE_URL_CHARS || lines.size <= 1) return url
            lines = lines.dropLast(maxOf(1, lines.size / 10))
            shortened = true
        }
    }

    private fun encode(text: String) = URLEncoder.encode(text, Charsets.UTF_8.name())

    // Kept out of backups: a crash report is only for the device it happened on.
    private fun file(context: Context) = File(context.noBackupFilesDir, FILE_NAME)

    fun save(context: Context, report: String) = file(context).writeText(report.take(MAX_REPORT_CHARS))

    fun pending(context: Context): String? =
        runCatching {
                val file = file(context)
                if (!file.isFile) return null
                file.bufferedReader().use { reader ->
                    val buffer = CharArray(MAX_REPORT_CHARS)
                    var total = 0
                    while (total < buffer.size) {
                        val count = reader.read(buffer, total, buffer.size - total)
                        if (count < 0) break
                        total += count
                    }
                    String(buffer, 0, total)
                }
            }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }

    fun clear(context: Context) {
        file(context).delete()
    }
}
