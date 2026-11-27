package com.anyremote.remote.service

internal object RemoteName {
    const val MAX_LENGTH = 40

    fun length(name: String): Int = name.trim().let { it.codePointCount(0, it.length) }

    fun isValid(name: String): Boolean = length(name) in 1..MAX_LENGTH
}
