package com.anyremote.remote.service

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import com.anyremote.remote.MainActivity
import com.anyremote.remote.R
import java.util.concurrent.Executors
import kotlin.math.abs

object ShortcutHelper {
    private const val TAG = "ShortcutHelper"
    private const val ICON_SIZE = 192
    private val shortcutExecutor = Executors.newSingleThreadExecutor()

    private fun shortcutId(deviceId: Int, brandId: Int) = "remote_${deviceId}_$brandId"

    fun syncSavedShortcut(context: Context, deviceId: Int, brandId: Int) {
        val app = context.applicationContext
        shortcutExecutor.execute {
            val manager = app.getSystemService(ShortcutManager::class.java) ?: return@execute
            val ids = listOf(shortcutId(deviceId, brandId))
            try {
                if (FavoriteManager.isFavorite(app, deviceId, brandId)) {
                    manager.enableShortcuts(ids)
                } else {
                    manager.disableShortcuts(ids, app.getText(R.string.remote_removed))
                }
            } catch (error: RuntimeException) {
                Log.w(TAG, "Updating shortcut availability failed", error)
            }
        }
    }

    /** Asks the launcher to pin a home-screen shortcut that opens this remote. */
    fun pinRemoteShortcut(
        context: Context,
        deviceId: Int,
        brandId: Int,
        deviceName: String,
        brandName: String,
    ) {
        val app = context.applicationContext
        shortcutExecutor.execute {
            if (!FavoriteManager.isFavorite(app, deviceId, brandId)) return@execute
            val message = try {
                when {
                    !ShortcutManagerCompat.isRequestPinShortcutSupported(context) ->
                        context.getString(R.string.shortcut_not_supported)
                    ShortcutManagerCompat.requestPinShortcut(
                        context, buildShortcut(context, deviceId, brandId, deviceName, brandName), null) ->
                        context.getString(R.string.shortcut_adding, "$brandName $deviceName")
                    else -> context.getString(R.string.shortcut_not_supported)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pinning shortcut failed", e)
                context.getString(R.string.shortcut_failed)
            }
            ContextCompat.getMainExecutor(app).execute {
                Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Adds this remote to the launcher's long-press menu; the system trims to its own limit. */
    fun registerDynamicShortcuts(
        context: Context,
        deviceId: Int,
        brandId: Int,
        deviceName: String,
        brandName: String,
    ) {
        val app = context.applicationContext
        shortcutExecutor.execute {
            if (!FavoriteManager.isFavorite(app, deviceId, brandId)) return@execute
            try {
                ShortcutManagerCompat.pushDynamicShortcut(
                    app, buildShortcut(app, deviceId, brandId, deviceName, brandName))
            } catch (e: RuntimeException) {
                Log.w(TAG, "Updating dynamic shortcuts failed", e)
            }
        }
    }

    private fun buildShortcut(
        context: Context,
        deviceId: Int,
        brandId: Int,
        deviceName: String,
        brandName: String,
    ): ShortcutInfoCompat {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("deviceId", deviceId)
                putExtra("brandId", brandId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return ShortcutInfoCompat.Builder(context, shortcutId(deviceId, brandId))
            .setShortLabel("$brandName $deviceName")
            .setLongLabel(context.getString(R.string.shortcut_long_label, brandName, deviceName))
            .setIcon(IconCompat.createWithAdaptiveBitmap(letterIcon(brandName)))
            .setIntent(intent)
            .build()
    }

    /** Round colored badge with the brand's first letter; hue is stable per name. */
    private fun letterIcon(text: String): Bitmap {
        val size = ICON_SIZE.toFloat()
        val bitmap = createBitmap(ICON_SIZE, ICON_SIZE)
        val canvas = Canvas(bitmap)
        val hue = abs(text.hashCode() % 360).toFloat()
        val fill =
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.HSVToColor(floatArrayOf(hue, 0.6f, 0.9f)) }
        canvas.drawCircle(size / 2, size / 2, size / 2, fill)

        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = size * 0.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
        val letter = text.take(1).uppercase().ifEmpty { "R" }
        val bounds = Rect().also { textPaint.getTextBounds(letter, 0, letter.length, it) }
        canvas.drawText(letter, size / 2, size / 2 - bounds.exactCenterY(), textPaint)
        return bitmap
    }
}
