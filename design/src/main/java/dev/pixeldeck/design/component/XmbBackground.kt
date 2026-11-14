package dev.pixeldeck.design.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin

enum class XmbBackgroundStyle { WAVES, AURORA, SOLID }

@Composable
fun XmbBackground(
    modifier: Modifier = Modifier,
    accent: Color,
    secondary: Color,
    style: XmbBackgroundStyle = XmbBackgroundStyle.WAVES,
    intensity: Float = 0.7f,
    speed: Float = 1f,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val base = MaterialTheme.colorScheme.background
    val deep = base.copy(
        red = (base.red * 0.55f).coerceIn(0f, 1f),
        green = (base.green * 0.55f).coerceIn(0f, 1f),
        blue = (base.blue * 0.65f).coerceIn(0f, 1f),
    )
    val duration = (16_000 / speed.coerceIn(0.15f, 3f)).toInt().coerceIn(3_000, 90_000)
    val transition = rememberInfiniteTransition(label = "xmb-waves")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "xmb-phase",
    )

    Box(
        modifier = modifier.drawBehind {
            drawRect(Brush.verticalGradient(0f to deep, 0.55f to base, 1f to deep))

            when (style) {
                XmbBackgroundStyle.WAVES -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.16f * intensity), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.22f),
                            radius = size.width * 0.9f,
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(size.width * 0.8f, size.height * 0.22f),
                    )

                    val ribbons = 5
                    for (i in 0 until ribbons) {
                        val baseY = size.height * (0.42f + i * 0.072f)
                        val amplitude = size.height * 0.055f * intensity * (1f - i * 0.12f)
                        val path = Path()
                        val steps = 56
                        for (step in 0..steps) {
                            val x = size.width * step / steps
                            val t = step.toFloat() / steps
                            val y = baseY +
                                sin(t * PI.toFloat() * 2.6f + phase + i * 0.65f) * amplitude +
                                sin(t * PI.toFloat() * 1.1f - phase * 1.35f + i) * amplitude * 0.55f
                            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        val ribbonColor = if (i % 2 == 0) accent else secondary
                        val stroke = (size.height * 0.013f).coerceAtLeast(2f)
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.18f to ribbonColor.copy(alpha = 0.10f * intensity),
                                0.5f to ribbonColor.copy(alpha = 0.34f * intensity),
                                0.82f to ribbonColor.copy(alpha = 0.10f * intensity),
                                1f to Color.Transparent,
                            ),
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        if (glow) {
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.05f * intensity),
                                style = Stroke(width = stroke * 0.34f, cap = StrokeCap.Round),
                            )
                        }
                    }
                }

                XmbBackgroundStyle.AURORA -> {
                    val drift = sin(phase) * size.width * 0.06f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.24f * intensity), Color.Transparent),
                            center = Offset(size.width * 0.82f + drift, size.height * 0.18f),
                            radius = size.width * 1.05f,
                        ),
                        radius = size.width * 1.05f,
                        center = Offset(size.width * 0.82f + drift, size.height * 0.18f),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = 0.18f * intensity), Color.Transparent),
                            center = Offset(-size.width * 0.12f - drift, size.height * 0.72f),
                            radius = size.width * 0.9f,
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(-size.width * 0.12f - drift, size.height * 0.72f),
                    )
                }

                XmbBackgroundStyle.SOLID -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.10f * intensity), Color.Transparent),
                            center = Offset(size.width * 0.85f, 0f),
                            radius = size.width * 0.85f,
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.85f, 0f),
                    )
                }
            }

            drawRect(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.62f to Color.Transparent,
                    1f to deep.copy(alpha = 0.92f),
                ),
            )
        },
        content = content,
    )
}
