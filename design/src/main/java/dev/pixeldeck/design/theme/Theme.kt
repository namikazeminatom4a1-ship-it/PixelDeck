package dev.pixeldeck.design.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val PixelShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun PixelDeckTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    accent: Color? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            if (accent != null) {
                base.copy(
                    primary = accent,
                    primaryContainer = accent.copy(alpha = 0.22f),
                    onPrimaryContainer = if (darkTheme) Color.White else Color(0xFF140B30),
                )
            } else {
                base
            }
        }

        darkTheme -> if (accent != null) {
            DarkColors.copy(
                primary = accent,
                primaryContainer = accent.copy(alpha = 0.22f),
                onPrimaryContainer = Color.White,
            )
        } else {
            DarkColors
        }

        else -> if (accent != null) {
            LightColors.copy(
                primary = accent,
                primaryContainer = accent.copy(alpha = 0.18f),
                onPrimaryContainer = Color(0xFF140B30),
            )
        } else {
            LightColors
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = PixelTypography,
        shapes = PixelShapes,
        content = content,
    )
}
