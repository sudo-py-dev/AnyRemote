package com.anyremote.remote.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal object GitHubUpdates {
    private const val REPO = "sudo-py-dev/AnyRemote"
    private const val MAX_APK_BYTES = 100L * 1024 * 1024
    private val versionPattern = Regex("[0-9]{1,9}\\.[0-9]{1,9}\\.[0-9]{1,9}")
    private val sha256Pattern = Regex("[0-9a-f]{64}")
    private val allowedHosts =
        setOf(
            "api.github.com",
            "github.com",
            "release-assets.githubusercontent.com",
            "objects.githubusercontent.com",
        )

    data class Release(val version: String) {
        val apkName
            get() = "AnyRemote-$version.apk"

        val url
            get() = "https://github.com/$REPO/releases/download/v$version/$apkName"
    }

    suspend fun latest(installedVersion: String): Release? =
        withContext(Dispatchers.IO) {
            val endpoint = "https://api.github.com/repos/$REPO/releases/latest"
            val release = response(endpoint) { JSONObject(it.readBoundedText(1024 * 1024)) }
            val version = release.getString("tag_name").removePrefix("v")
            if (
                release.optBoolean("draft") ||
                    release.optBoolean("prerelease") ||
                    !isNewer(version, installedVersion)
            )
                return@withContext null

            val update = Release(version)
            val assets = release.getJSONArray("assets")
            val names = (0 until assets.length()).map { assets.getJSONObject(it).getString("name") }
            if (update.apkName in names && "${update.apkName}.sha256" in names) update else null
        }

    suspend fun download(context: Context, release: Release): File =
        withContext(Dispatchers.IO) {
            val checksum = response("${release.url}.sha256") { it.readBoundedText(1024) }.trim()
            val expected = checksum.substringBefore(' ')
            if (
                !sha256Pattern.matches(expected) ||
                    checksum.substringAfter(' ', "").trim() != release.apkName
            ) {
                throw IOException("Invalid release checksum")
            }

            val directory = File(context.cacheDir, "updates")
            if (!directory.isDirectory && !directory.mkdirs())
                throw IOException("Cannot create update cache")
            val temporary = File.createTempFile("update-", ".apk", directory)
            val jobContext = coroutineContext
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                response(release.url) { input ->
                    temporary.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            jobContext.ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > MAX_APK_BYTES)
                                throw IOException("APK exceeds download limit")
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                    }
                }
                val actual = digest.digest().joinToString("") { "%02x".format(it) }
                if (actual != expected) throw IOException("APK checksum mismatch")
                val packages = context.packageManager
                val archive =
                    packages.getPackageArchiveInfo(temporary.path, 0)
                        ?: throw IOException("Invalid APK")
                @Suppress("DEPRECATION")
                val installed = packages.getPackageInfo(context.packageName, 0)
                if (
                    archive.packageName != context.packageName ||
                        archive.versionName != release.version ||
                        PackageInfoCompat.getLongVersionCode(archive) <=
                            PackageInfoCompat.getLongVersionCode(installed)
                )
                    throw IOException("APK does not update this installation")

                val verified = File(directory, release.apkName)
                if (!temporary.renameTo(verified)) throw IOException("Cannot save verified APK")
                directory
                    .listFiles()
                    ?.filter { it != verified && it.name.startsWith("AnyRemote-") }
                    ?.forEach { it.delete() }
                verified
            } finally {
                temporary.delete()
            }
        }

    internal fun isNewer(candidate: String, installed: String): Boolean {
        if (!versionPattern.matches(candidate) || !versionPattern.matches(installed)) return false
        val left = candidate.split('.').map(String::toInt)
        val right = installed.split('.').map(String::toInt)
        return left.indices.firstOrNull { left[it] != right[it] }?.let { left[it] > right[it] }
            ?: false
    }

    private fun <T> response(address: String, consume: (InputStream) -> T): T {
        var url = URL(address)
        repeat(5) {
            if (url.protocol != "https" || url.host !in allowedHosts)
                throw IOException("Unexpected update host")
            val connection = url.openConnection() as HttpsURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            try {
                when (val status = connection.responseCode) {
                    200 -> return connection.inputStream.use(consume)
                    301,
                    302,
                    303,
                    307,
                    308 -> {
                        val location =
                            connection.getHeaderField("Location")
                                ?: throw IOException("Missing redirect")
                        url = URL(url, location)
                    }
                    else -> throw IOException("GitHub returned HTTP $status")
                }
            } finally {
                connection.disconnect()
            }
        }
        throw IOException("Too many update redirects")
    }

    private fun InputStream.readBoundedText(limit: Int): String {
        val bytes = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val count = read(buffer)
            if (count < 0) return bytes.toString(Charsets.UTF_8.name())
            if (bytes.size() + count > limit) throw IOException("Update response exceeds limit")
            bytes.write(buffer, 0, count)
        }
    }
}
