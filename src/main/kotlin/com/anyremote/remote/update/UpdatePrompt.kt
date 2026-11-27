package com.anyremote.remote.update

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.anyremote.remote.BuildConfig
import com.anyremote.remote.R
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun UpdatePrompt() {
    if (!UpdateState.enabled) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var verifiedApk by remember { mutableStateOf<File?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    // Debug builds are signed with a different key, so only release builds check on launch.
    // A failed check stays silent here; Settings shows it and can retry.
    LaunchedEffect(Unit) {
        if (!BuildConfig.DEBUG && UpdateState.status == UpdateState.Status.Idle) {
            UpdateState.check()
        }
    }

    fun install(apk: File) {
        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.fromParts("package", context.packageName, null),
                    )
                )
                return
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
            val installIntent =
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
            val installer =
                context.packageManager
                    .activitiesFor(
                        installIntent,
                        PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY,
                    )
                    .firstOrNull { it.activityInfo.exported }
                    ?.activityInfo
                    ?: throw ActivityNotFoundException("No system APK installer found")
            installIntent.setClassName(installer.packageName, installer.name)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(installIntent)
            UpdateState.hidePrompt()
        } catch (_: ActivityNotFoundException) {
            failed = true
        } catch (_: SecurityException) {
            failed = true
        }
    }

    val update = UpdateState.release?.takeIf { UpdateState.promptVisible } ?: return
    val readyApk = verifiedApk?.takeIf { it.name == update.apkName && it.isFile }
    val dismiss = { if (!downloading) UpdateState.hidePrompt() }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(R.string.update_available, update.version)) },
        text = {
            when {
                downloading -> LinearProgressIndicator()
                failed -> Text(stringResource(R.string.update_failed))
                readyApk != null -> Text(stringResource(R.string.update_ready))
                else -> Text(stringResource(R.string.update_available_detail))
            }
        },
        confirmButton = {
            TextButton(
                enabled = !downloading,
                onClick = {
                    failed = false
                    val apk = readyApk
                    if (apk != null) {
                        install(apk)
                    } else {
                        downloading = true
                        scope.launch {
                            try {
                                verifiedApk =
                                    GitHubUpdates.download(context.applicationContext, update)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                failed = true
                            } finally {
                                downloading = false
                            }
                        }
                    }
                },
            ) {
                Text(
                    stringResource(
                        if (readyApk != null) R.string.update_install
                        else R.string.update_download
                    )
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !downloading, onClick = dismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
