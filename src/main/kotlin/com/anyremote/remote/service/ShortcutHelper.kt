package com.anyremote.remote.service

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.anyremote.remote.MainActivity

object ShortcutHelper {
    private fun createLetterIcon(text: String): android.graphics.Bitmap {
        val size = 192
        val bitmap =
            android.graphics.Bitmap.createBitmap(
                size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                val hash = text.hashCode()
                val h = Math.abs(hash) % 360f
                color = android.graphics.Color.HSVToColor(floatArrayOf(h, 0.6f, 0.9f))
                style = android.graphics.Paint.Style.FILL
            }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        val textPaint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = size * 0.5f
                typeface =
                    android.graphics.Typeface.create(
                        android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
        val letter = if (text.isNotEmpty()) text.substring(0, 1).uppercase() else "R"
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(letter, 0, letter.length, textBounds)
        val y = (size / 2f) - textBounds.exactCenterY()
        canvas.drawText(letter, size / 2f, y, textPaint)
        return bitmap
    }

    fun pinRemoteShortcut(
        context: Context,
        deviceId: Int,
        brandId: Int,
        deviceName: String,
        brandName: String,
    ) {
        try {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val shortcutId = "remote_${deviceId}_$brandId"

                val intent =
                    Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("deviceId", deviceId)
                        putExtra("brandId", brandId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                val bitmap = createLetterIcon(brandName)
                val iconCompat = IconCompat.createWithAdaptiveBitmap(bitmap)

                val shortcut =
                    ShortcutInfoCompat.Builder(context, shortcutId)
                        .setShortLabel("$brandName $deviceName")
                        .setLongLabel("$brandName $deviceName Remote")
                        .setIcon(iconCompat)
                        .setIntent(intent)
                        .build()

                val success = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
                if (success) {
                    Toast.makeText(
                            context, "Adding shortcut: $brandName $deviceName", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                            context, "Launcher pinning not supported by system", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                        context,
                        "Home screen shortcut pinning is not supported",
                        Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                    context, "Failed to pin shortcut: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun registerDynamicShortcuts(
        context: Context,
        deviceId: Int,
        brandId: Int,
        deviceName: String,
        brandName: String,
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                val shortcutManager =
                    context.getSystemService(ShortcutManager::class.java) ?: return

                val shortcutId = "remote_${deviceId}_$brandId"
                val intent =
                    Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("deviceId", deviceId)
                        putExtra("brandId", brandId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                val bitmap = createLetterIcon(brandName)
                val shortcutIcon =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Icon.createWithAdaptiveBitmap(bitmap)
                    } else {
                        Icon.createWithBitmap(bitmap)
                    }

                val shortcut =
                    ShortcutInfo.Builder(context, shortcutId)
                        .setShortLabel("$brandName $deviceName")
                        .setLongLabel("$brandName $deviceName Remote")
                        .setIcon(shortcutIcon)
                        .setIntent(intent)
                        .build()

                val existing = shortcutManager.dynamicShortcuts
                val updated = existing.toMutableList()
                updated.removeAll { it.id == shortcutId }
                updated.add(0, shortcut)

                if (updated.size > 4) {
                    shortcutManager.dynamicShortcuts = updated.take(4)
                } else {
                    shortcutManager.dynamicShortcuts = updated
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
