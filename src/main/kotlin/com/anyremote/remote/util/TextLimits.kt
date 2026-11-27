package com.anyremote.remote.util

/** Keep whole Unicode code points, inspecting at most [limit] of them. */
internal fun String.limitCodePoints(limit: Int): String {
    if (length <= limit) return this
    var end = 0
    repeat(limit) {
        if (end == length) return this
        end += Character.charCount(codePointAt(end))
    }
    return substring(0, end)
}
