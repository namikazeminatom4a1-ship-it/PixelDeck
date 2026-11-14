package dev.pixeldeck.ui.common

import androidx.compose.ui.graphics.Color
import dev.pixeldeck.data.model.GameSystem
import java.util.Locale
import kotlin.math.roundToInt

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "Unknown size"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index++
    }
    return if (index == 0) {
        "${bytes} B"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[index])
    }
}

fun formatPlayTime(millis: Long): String {
    if (millis < 60_000) return "Not played yet"
    val minutes = (millis / 60_000.0).roundToInt()
    return when {
        minutes < 60 -> "$minutes min played"
        else -> {
            val hours = minutes / 60
            val rest = minutes % 60
            if (rest == 0) "$hours h played" else "$hours h $rest min played"
        }
    }
}

fun GameSystem.accentColor(): Color = Color(accentSeed)
