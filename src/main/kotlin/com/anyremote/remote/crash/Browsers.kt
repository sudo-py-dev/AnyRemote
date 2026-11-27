package com.anyremote.remote.crash

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import com.anyremote.remote.update.activitiesFor
import com.anyremote.remote.update.defaultActivityFor

/**
 * Opens links in a web browser only. Apps that claim a site's links, such as the GitHub app for
 * github.com, are left out: the GitHub app shows the issue form but ignores the prefilled report.
 */
internal object Browsers {
    // A browser opens any website; other apps only claim their own sites' links.
    private val probe =
        Intent(Intent.ACTION_VIEW, "https://example.com/".toUri())
            .addCategory(Intent.CATEGORY_BROWSABLE)

    /**
     * An intent that opens [url] in the default browser, in the only browser, or in a chooser that
     * lists only browsers. Null when no browser is installed or they can't be listed.
     */
    fun intentFor(context: Context, url: Uri, chooserTitle: CharSequence): Intent? {
        val packages = runCatching { browserPackages(context) }.getOrDefault(emptyList())
        if (packages.isEmpty()) return null
        val default = runCatching { defaultBrowser(context) }.getOrNull()?.takeIf { it in packages }
        val targets =
            (if (default != null) listOf(default) else packages).map { browser ->
                Intent(Intent.ACTION_VIEW, url)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setPackage(browser)
            }
        if (targets.size == 1) return targets.single()
        return Intent.createChooser(targets.first(), chooserTitle)
            .putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.drop(1).toTypedArray())
    }

    private fun browserPackages(context: Context): List<String> =
        context.packageManager
            .activitiesFor(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()

    /** The user's default browser, or null when Android would ask which app to use. */
    private fun defaultBrowser(context: Context): String? =
        context.packageManager.defaultActivityFor(probe)?.activityInfo?.packageName
}
