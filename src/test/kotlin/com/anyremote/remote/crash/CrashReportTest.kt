package com.anyremote.remote.crash

import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReportTest {
    private fun report(error: Throwable) =
        CrashReport.format(
            appVersion = "0.0.4 (4)",
            android = "14 (API 34)",
            device = "Google Pixel 8",
            mainThread = true,
            error = error,
        )

    private fun body(url: String) =
        URLDecoder.decode(url.substringAfter("&report="), Charsets.UTF_8.name())

    @Test
    fun reportHasOnlyVersionsDeviceAndTrace() {
        val text = report(IllegalStateException("boom"))
        val header = text.substringBefore("\n\n").lines()
        assertEquals(
            listOf(
                "AnyRemote 0.0.4 (4)",
                "Android 14 (API 34)",
                "Device: Google Pixel 8",
                "Thread: main",
            ),
            header,
        )
        val trace = text.substringAfter("\n\n").lines()
        assertEquals("java.lang.IllegalStateException: boom", trace.first())
        assertTrue(trace.drop(1).all { it.startsWith("\tat ") || it.startsWith("\t…") })
    }

    @Test
    fun scrubRemovesUserData() {
        val message =
            "Remote \"Living room TV\" failed for jane.doe@example.com at " +
                "https://example.com/u/123?token=abc in /data/user/0/com.anyremote.remote/files/a.json " +
                "from 192.168.1.20 (AA:BB:CC:DD:EE:FF) phone 0541234567 named 'Den'"
        val scrubbed = CrashReport.scrub(message)
        listOf("Living room", "jane", "example.com", "token", "/data/", "192.168", "AA:BB", "0541234567", "Den")
            .forEach { assertFalse("leaked $it in $scrubbed", scrubbed.contains(it)) }
        assertTrue(scrubbed.contains("\"<text>\""))
        assertTrue(scrubbed.contains("<email>"))
        assertTrue(scrubbed.contains("<link>"))
        assertTrue(scrubbed.contains("<path>"))
    }

    @Test
    fun scrubKeepsUsefulMessages() {
        assertEquals(
            "Index 5 out of bounds for length 3",
            CrashReport.scrub("Index 5 out of bounds for length 3"),
        )
    }

    @Test
    fun messagesCannotAddLinesOrGrowWithoutLimit() {
        val text = report(IllegalStateException("one\nFake: line\r\n" + "x".repeat(5000)))
        val first = text.substringAfter("\n\n").lines().first()
        assertFalse(text.contains("\nFake: line"))
        assertTrue(first.length <= "java.lang.IllegalStateException: ".length + CrashReport.MAX_MESSAGE_CHARS)
    }

    @Test
    fun traceIsBoundedInFramesAndCauses() {
        var error: Throwable = IllegalStateException("root")
        repeat(20) { error = RuntimeException("level $it", error) }
        error.stackTrace =
            Array(500) { StackTraceElement("com.anyremote.remote.Deep", "call$it", "Deep.kt", it) }
        val text = report(error)
        assertTrue(text.length <= CrashReport.MAX_REPORT_CHARS)
        assertEquals(CrashReport.MAX_CAUSES - 1, text.lines().count { it.startsWith("Caused by: ") })
        assertTrue(text.contains("\t… ${500 - CrashReport.MAX_FRAMES} more"))
        assertTrue(text.contains("… more causes omitted"))
    }

    @Test
    fun causeLoopsEnd() {
        val first = IllegalStateException("first")
        val second = IllegalArgumentException("second", first)
        first.initCause(second)
        val text = report(first)
        assertEquals(1, text.lines().count { it.startsWith("Caused by: ") })
    }

    @Test
    fun titleNamesTheRootCause() {
        val error = RuntimeException("wrapper", IllegalArgumentException("bad brand id"))
        assertEquals("Crash: IllegalArgumentException: bad brand id", CrashReport.title(report(error)))
        assertEquals("Crash: NullPointerException", CrashReport.title(report(NullPointerException())))
    }

    @Test
    fun shortIssueLinkKeepsWholeReport() {
        val text = report(IllegalStateException("boom"))
        val url = CrashReport.issueUrl(text)
        assertTrue(
            url.startsWith(
                "https://github.com/sudo-py-dev/AnyRemote/issues/new?template=crash_report.yml&title="
            )
        )
        assertEquals(text, body(url))
    }

    @Test
    fun longIssueLinkIsShortenedToFit() {
        var error: Throwable = IllegalStateException("deep")
        repeat(4) { error = RuntimeException("wrap $it", error) }
        val url = CrashReport.issueUrl(report(error))
        assertTrue(url.length <= CrashReport.MAX_ISSUE_URL_CHARS)
        assertTrue(body(url).contains("java.lang.RuntimeException: wrap 3"))
        assertTrue(body(url).contains(CrashReport.TRUNCATED_NOTE))
    }
}
