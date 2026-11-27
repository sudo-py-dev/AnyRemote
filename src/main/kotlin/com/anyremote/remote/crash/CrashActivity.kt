package com.anyremote.remote.crash

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.anyremote.remote.MainActivity
import com.anyremote.remote.R
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.LocaleManager
import com.anyremote.remote.ui.theme.AnyRemoteTheme

/** Shows the report for a crash and lets the user copy it or open a GitHub issue with it. */
class CrashActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = runCatching { LocaleManager.getSavedLanguage(newBase) }.getOrDefault("system")
        super.attachBaseContext(LocaleManager.applyLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val report = CrashReport.pending(this)
        if (report == null) {
            finish()
            return
        }
        val theme = screenTheme()
        val barStyle =
            if (theme.dark) SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        setContent {
            AnyRemoteTheme(darkTheme = theme.dark, dynamicColor = theme.dynamicColor) {
                CrashScreen(
                    report = report,
                    onReport = { report(report) },
                    onCopy = { copy(report) },
                    onRestart = ::restart,
                    onClose = ::close,
                )
            }
        }
    }

    private class ScreenTheme(val dark: Boolean, val dynamicColor: Boolean)

    /**
     * The theme chosen in Settings, so this screen looks like the rest of the app. This process
     * starts fresh after a crash, so the settings are read here. If they can't be read, or the
     * device's dynamic colors fail, it falls back to the device's own dark mode and the app's
     * built-in colors, so a broken setting can't break the crash screen too.
     */
    private fun screenTheme(): ScreenTheme {
        val systemDark =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val (dark, wantsDynamic) =
            runCatching {
                    FavoriteManager.init(this)
                    val dark =
                        when (FavoriteManager.currentThemeState.value) {
                            "light" -> false
                            "dark" -> true
                            else -> systemDark
                        }
                    dark to FavoriteManager.currentDynamicColorState.value
                }
                .getOrDefault(systemDark to true)
        return ScreenTheme(dark, dynamicColor = wantsDynamic && dynamicColorsWork(dark))
    }

    /** Some devices fail to build a dynamic color scheme; check before the screen uses it. */
    private fun dynamicColorsWork(dark: Boolean): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            runCatching {
                    if (dark) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                }
                .isSuccess

    override fun onDestroy() {
        // Leaving the screen any way (including Back) means the report has been seen.
        if (isFinishing) CrashReport.clear(this)
        super.onDestroy()
    }

    private fun copy(report: String, announce: Boolean = true) {
        val clipboard = getSystemService(ClipboardManager::class.java) ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.crash_report), report))
        // Android 13 and later show their own confirmation when something is copied.
        if (announce && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, R.string.crash_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun report(report: String) {
        // Long traces are shortened to fit the link, so the full report goes on the clipboard.
        copy(report, announce = false)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, CrashReport.issueUrl(report).toUri()))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.crash_no_browser, Toast.LENGTH_LONG).show()
        }
    }

    private fun restart() {
        CrashReport.clear(this)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun close() {
        CrashReport.clear(this)
        finish()
    }

    companion object {
        fun intent(context: Context) =
            Intent(context, CrashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

@Composable
private fun CrashScreen(
    report: String,
    onReport: () -> Unit,
    onCopy: () -> Unit,
    onRestart: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Column(
                modifier =
                    Modifier.weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Outlined.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 24.dp).size(56.dp),
                )
                Text(
                    stringResource(R.string.crash_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.crash_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // The report is English code, so it reads left to right even in Hebrew or
                    // Arabic. Otherwise each line is right-aligned and its start scrolled off.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        SelectionContainer {
                            Text(
                                report,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                softWrap = false,
                                modifier =
                                    Modifier.horizontalScroll(rememberScrollState())
                                        .padding(16.dp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.crash_report_github))
                }
                OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.crash_copy))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onClose) { Text(stringResource(R.string.crash_close)) }
                    TextButton(onClick = onRestart) { Text(stringResource(R.string.crash_restart)) }
                }
            }
        }
    }
}
