package dev.pixeldeck.ui.theme

import androidx.compose.ui.graphics.Color
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.ThemePreset

data class DeckPalette(
    val accent: Color,
    val secondary: Color,
)

fun AppSettings.palette(): DeckPalette = DeckPalette(
    accent = accentFor(themePreset, accentHue),
    secondary = secondaryFor(themePreset, accentHue),
)

fun accentFor(preset: ThemePreset, hue: Float): Color {
    val safeHue = ((hue % 360f) + 360f) % 360f
    return when (preset) {
        ThemePreset.MIDNIGHT -> Color(0xFFB9C9E8)
        ThemePreset.OCEAN -> Color(0xFF3AA7FF)
        ThemePreset.SUNSET -> Color(0xFFFF7A45)
        ThemePreset.EMERALD -> Color(0xFF3EDC97)
        ThemePreset.VIOLET -> Color(0xFF9B6CFF)
        ThemePreset.ROSE -> Color(0xFFFF5C9E)
        ThemePreset.CYAN -> Color(0xFF2DE2E6)
        ThemePreset.GOLD -> Color(0xFFFFC24B)
        ThemePreset.CUSTOM -> Color.hsv(safeHue, 0.72f, 1f)
    }
}

fun secondaryFor(preset: ThemePreset, hue: Float): Color {
    val safeHue = ((hue % 360f) + 360f) % 360f
    return when (preset) {
        ThemePreset.MIDNIGHT -> Color(0xFF6F87B5)
        ThemePreset.OCEAN -> Color(0xFF2DE2E6)
        ThemePreset.SUNSET -> Color(0xFFFFC24B)
        ThemePreset.EMERALD -> Color(0xFF9BE564)
        ThemePreset.VIOLET -> Color(0xFFF272B6)
        ThemePreset.ROSE -> Color(0xFFFF9E7A)
        ThemePreset.CYAN -> Color(0xFF6C8CFF)
        ThemePreset.GOLD -> Color(0xFFFF8A65)
        ThemePreset.CUSTOM -> Color.hsv((safeHue + 52f) % 360f, 0.62f, 1f)
    }
}

fun ThemePreset.label(): String = when (this) {
    ThemePreset.MIDNIGHT -> "Midnight"
    ThemePreset.OCEAN -> "Ocean"
    ThemePreset.SUNSET -> "Sunset"
    ThemePreset.EMERALD -> "Emerald"
    ThemePreset.VIOLET -> "Violet"
    ThemePreset.ROSE -> "Rose"
    ThemePreset.CYAN -> "Cyan"
    ThemePreset.GOLD -> "Gold"
    ThemePreset.CUSTOM -> "Custom"
}
