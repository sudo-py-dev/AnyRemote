package com.anyremote.remote.update

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

// Android 13 replaced the int-flag PackageManager methods with typed flag objects. The typed
// versions don't exist before Android 13, so older devices keep the int overloads.

internal fun PackageManager.archiveInfo(path: String): PackageInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(0))
    } else {
        legacyArchiveInfo(path)
    }

internal fun PackageManager.activitiesFor(intent: Intent, flags: Int): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        legacyActivitiesFor(intent, flags)
    }

@Suppress("DEPRECATION")
private fun PackageManager.legacyArchiveInfo(path: String): PackageInfo? =
    getPackageArchiveInfo(path, 0)

@Suppress("DEPRECATION")
private fun PackageManager.legacyActivitiesFor(intent: Intent, flags: Int): List<ResolveInfo> =
    queryIntentActivities(intent, flags)

internal fun PackageManager.defaultActivityFor(intent: Intent): ResolveInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveActivity(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
        )
    } else {
        legacyDefaultActivityFor(intent)
    }

@Suppress("DEPRECATION")
private fun PackageManager.legacyDefaultActivityFor(intent: Intent): ResolveInfo? =
    resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
