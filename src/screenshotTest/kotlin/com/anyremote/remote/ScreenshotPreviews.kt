package com.anyremote.remote

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.ir.AcState
import com.anyremote.remote.ui.components.BrandSelectionPanel
import com.anyremote.remote.ui.components.CategorySelectionGrid
import com.anyremote.remote.ui.components.RemoteDefaults
import com.anyremote.remote.ui.layouts.AcLayout
import com.anyremote.remote.ui.screens.*
import com.anyremote.remote.ui.theme.AnyRemoteTheme

/*
 * Renders the app's screens to PNGs for UI review (screenshot test suite, run in CI).
 * Each preview comes in light and dark; tall ones show the whole scrollable remote.
 */

private const val PHONE = "spec:width=411dp,height=891dp"
private const val TALL = "spec:width=411dp,height=1300dp"

@Preview(name = "light", device = PHONE, showBackground = true)
@Preview(
    name = "dark",
    device = PHONE,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PhonePreviews

@Preview(name = "light", device = TALL, showBackground = true)
@Preview(
    name = "dark",
    device = TALL,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TallPreviews

@Preview(name = "arabic", device = PHONE, showBackground = true, locale = "ar")
@Preview(name = "hebrew", device = PHONE, showBackground = true, locale = "iw")
annotation class RtlPhonePreviews

private fun brands(vararg names: String) = names.mapIndexed { i, n ->
    Brand(id = i + 1, nameEn = n, category = if (i < 3) "popular" else "other")
}

private val sampleDevices =
    listOf(
        DeviceType(
            1,
            "TV",
            brands(
                "Samsung",
                "LG",
                "Sony",
                "Panasonic",
                "Philips",
                "Hisense",
                "TCL",
                "Sharp",
                "Toshiba",
                "Xiaomi",
                "Vizio",
                "Grundig",
            ),
        ),
        DeviceType(2, "Set-top box", brands("Humax", "Arris")),
        DeviceType(3, "AC", brands("Gree", "Daikin", "Mitsubishi Electric", "LG", "Samsung")),
        DeviceType(4, "DVD Player", brands("Sony", "Pioneer")),
        DeviceType(6, "Fan", brands("Dyson", "Xiaomi")),
        DeviceType(8, "A/V receiver", brands("Yamaha", "Denon", "Onkyo")),
        DeviceType(10, "Projector", brands("Epson", "BenQ")),
        DeviceType(11, "Cable / Satellite box", brands("Sky", "Dish")),
        DeviceType(12, "Box", brands("Xiaomi Mi Box")),
        DeviceType(13, "Camera", brands("Canon", "Nikon")),
    )

@Composable
private fun Themed(content: @Composable () -> Unit) {
    RemoteDatabase.loadForPreview(sampleDevices)
    // Brand palette, not wallpaper colors, so screenshots are stable.
    AnyRemoteTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}

@Composable
private fun Remote(deviceId: Int) = Themed {
    RemoteControllerScreen(deviceId = deviceId, brandId = 1, onBackClick = {})
}

@PreviewTest @PhonePreviews @Composable fun TvRemote() = Remote(1)

@PreviewTest @TallPreviews @Composable fun TvRemoteFull() = Remote(1)

@PreviewTest @TallPreviews @Composable fun AcRemote() = Remote(3)

@PreviewTest
@TallPreviews
@Composable
fun AcRemoteOff() = Themed {
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(Modifier.widthIn(max = RemoteDefaults.ContentMaxWidth)) {
            AcLayout(state = AcState(power = false), onChange = {})
        }
    }
}

@PreviewTest @TallPreviews @Composable fun FanRemote() = Remote(6)

@PreviewTest @TallPreviews @Composable fun AudioRemote() = Remote(8)

@PreviewTest @TallPreviews @Composable fun ProjectorRemote() = Remote(10)

@PreviewTest @PhonePreviews @Composable fun CameraRemote() = Remote(13)

@PreviewTest @PhonePreviews @Composable fun GenericRemote() = Remote(99)

@PreviewTest
@PhonePreviews
@Composable
fun DashboardEmpty() = Themed {
    RemotesHome(
        remotes = emptyList(),
        showIrNotice = true,
        onDismissIrNotice = {},
        onRemoteClick = {},
        onRemoteOptions = {},
        onAddClick = {},
    )
}

@PreviewTest
@PhonePreviews
@Composable
fun DashboardWithRemotes() = Themed {
    val d = sampleDevices.associateBy { it.id }
    RemotesHome(
        remotes =
            listOf(
                SavedRemote(d.getValue(1), d.getValue(1).brands[0], "Living room TV"),
                SavedRemote(
                    d.getValue(3),
                    d.getValue(3).brands[2],
                    "Bedroom Mitsubishi Electric air conditioner",
                ),
                SavedRemote(d.getValue(6), d.getValue(6).brands[0], "Dyson Fan"),
                SavedRemote(d.getValue(8), d.getValue(8).brands[0], "Yamaha A/V"),
                SavedRemote(d.getValue(13), d.getValue(13).brands[0], "Canon Camera"),
            ),
        showIrNotice = false,
        onDismissIrNotice = {},
        onRemoteClick = {},
        onRemoteOptions = {},
        onAddClick = {},
    )
}

@PreviewTest
@PhonePreviews
@RtlPhonePreviews
@Composable
fun DeviceTypePicker() = Themed { CategorySelectionGrid(onCategoryClick = {}) }

@PreviewTest
@PhonePreviews
@RtlPhonePreviews
@Composable
fun BrandPicker() = Themed { BrandSelectionPanel(device = sampleDevices[0], onBrandClick = {}) }

@PreviewTest
@PhonePreviews
@RtlPhonePreviews
@Composable
fun PairingWizard() = Themed {
    MatchIRScreen(
        deviceId = 1,
        brandId = 1,
        onMatchSuccess = {},
        onSaveAndReturnToDashboard = {},
        onBackClick = {},
    )
}

@PreviewTest
@PhonePreviews
@RtlPhonePreviews
@Composable
fun RenameRemote() = Themed {
    EditRemoteScreen(
        device = sampleDevices[0],
        brand = sampleDevices[0].brands[0],
        onDismiss = {},
        onSave = {},
    )
}

@PreviewTest
@PhonePreviews
@RtlPhonePreviews
@Composable
fun Settings() = Themed { SettingsScreen(onBackClick = {}) }

// Right-to-left check: Hebrew is one of the supported languages.
@PreviewTest
@Preview(name = "arabic", device = TALL, showBackground = true, locale = "ar")
@Preview(name = "hebrew", device = TALL, showBackground = true, locale = "iw")
@Composable
fun TvRemoteRtl() = Remote(1)

@PreviewTest
@Preview(name = "hebrew", device = PHONE, showBackground = true, locale = "iw")
@Composable
fun DashboardRtl() = DashboardWithRemotes()

@PreviewTest
@Preview(name = "compact", device = "spec:width=320dp,height=640dp", fontScale = 1.3f)
@Preview(
    name = "compact-hebrew",
    device = "spec:width=320dp,height=640dp",
    locale = "iw",
    fontScale = 1.3f,
)
@Composable
fun MaximumRemoteNames() = Themed {
    val tv = sampleDevices.first()
    RemotesHome(
        remotes =
            listOf(
                SavedRemote(tv, tv.brands[0], "W".repeat(40)),
                SavedRemote(tv, tv.brands[1], "טלוויזיה בסלון ".repeat(3).take(40)),
            ),
        showIrNotice = false,
        onDismissIrNotice = {},
        onRemoteClick = {},
        onRemoteOptions = {},
        onAddClick = {},
    )
}
