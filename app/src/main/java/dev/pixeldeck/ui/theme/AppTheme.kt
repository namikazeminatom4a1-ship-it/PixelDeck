package dev.pixeldeck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.ThemeMode
import dev.pixeldeck.design.theme.PixelDeckTheme

@Composable
fun AppTheme(
    settings: AppSettings,
    accent: Color? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    PixelDeckTheme(
        darkTheme = darkTheme,
        dynamicColor = settings.dynamicColor,
        accent = accent ?: settings.palette().accent,
        content = content,
    )
}
