package dev.pixeldeck.design.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Violet = Color(0xFF8B6CFF)
val Cyan = Color(0xFF35D6E8)
val Pink = Color(0xFFF272B6)
val Mint = Color(0xFF5BE3A6)

val Ink = Color(0xFF08080C)
val InkElevated = Color(0xFF101017)
val InkSurface = Color(0xFF16161F)
val InkOutline = Color(0xFF2A2A38)
val Paper = Color(0xFFF7F6FB)
val PaperSurface = Color(0xFFFFFFFF)
val PaperOutline = Color(0xFFE3E0EC)

internal val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = Color(0xFF120A2E),
    primaryContainer = Color(0xFF2A2050),
    onPrimaryContainer = Color(0xFFE4DCFF),
    secondary = Cyan,
    onSecondary = Color(0xFF002F34),
    secondaryContainer = Color(0xFF113B40),
    onSecondaryContainer = Color(0xFFB6F4FB),
    tertiary = Pink,
    onTertiary = Color(0xFF33091F),
    tertiaryContainer = Color(0xFF43203A),
    onTertiaryContainer = Color(0xFFFFD6EA),
    background = Ink,
    onBackground = Color(0xFFEDEBF5),
    surface = InkElevated,
    onSurface = Color(0xFFEDEBF5),
    surfaceVariant = InkSurface,
    onSurfaceVariant = Color(0xFFB9B6C9),
    surfaceContainer = Color(0xFF13131B),
    surfaceContainerHigh = Color(0xFF1B1B25),
    outline = InkOutline,
    outlineVariant = Color(0xFF21212D),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3A0808),
)

internal val LightColors = lightColorScheme(
    primary = Color(0xFF6A4BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E0FF),
    onPrimaryContainer = Color(0xFF1D0B5A),
    secondary = Color(0xFF0E7C86),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB6F4FB),
    onSecondaryContainer = Color(0xFF002F34),
    tertiary = Color(0xFFB33E7E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD6EA),
    onTertiaryContainer = Color(0xFF3A0A23),
    background = Paper,
    onBackground = Color(0xFF1B1A22),
    surface = PaperSurface,
    onSurface = Color(0xFF1B1A22),
    surfaceVariant = Color(0xFFF0EDF6),
    onSurfaceVariant = Color(0xFF5E5B6B),
    surfaceContainer = Color(0xFFF3F0F9),
    surfaceContainerHigh = Color(0xFFEDEAF5),
    outline = PaperOutline,
    outlineVariant = Color(0xFFE9E6F0),
    error = Color(0xFFB3261E),
    onError = Color.White,
)
