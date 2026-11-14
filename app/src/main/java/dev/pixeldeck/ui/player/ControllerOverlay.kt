package dev.pixeldeck.ui.player

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.libretrodroid.GLRetroView
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.emulation.RetroKeys
import kotlin.math.roundToInt

private val AnalogSystems = setOf("psx", "n64", "psp", "dreamcast", "ps2", "nds")

@Composable
fun ControllerOverlay(
    view: GLRetroView,
    game: Game,
    settings: AppSettings,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit,
) {
    val alpha = settings.touchControlsOpacity.coerceIn(0.25f, 1f)
    val platformHaptics = LocalHapticFeedback.current
    val haptics = remember(platformHaptics, settings.hapticsEnabled) {
        if (settings.hapticsEnabled) {
            platformHaptics
        } else {
            object : HapticFeedback {
                override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
            }
        }
    }
    val sendKey: (Int, Int) -> Unit = { action, key ->
        view.sendKeyEvent(action, key)
        onInteraction()
    }

    CompositionLocalProvider(LocalHapticFeedback provides haptics) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            if (game.system.id in AnalogSystems) {
                VirtualStick(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = (-28).dp),
                    onMove = { x, y ->
                        view.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_LEFT, x, y)
                        onInteraction()
                    },
                )
            }

            Dpad(
                modifier = Modifier.align(Alignment.BottomStart),
                onKey = sendKey,
                alpha = alpha,
            )

            ActionCluster(
                modifier = Modifier.align(Alignment.BottomEnd),
                onKey = sendKey,
                alpha = alpha,
            )

            StartSelect(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                onKey = sendKey,
                alpha = alpha,
            )

            HoldButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(86.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                label = "L",
                alpha = alpha,
                onKey = sendKey,
                keyCode = RetroKeys.L,
            )

            HoldButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(86.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                label = "R",
                alpha = alpha,
                onKey = sendKey,
                keyCode = RetroKeys.R,
            )
        }
    }
    }
}

@Composable
private fun Dpad(
    onKey: (Int, Int) -> Unit,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val buttonSize = 58.dp
    Box(modifier = modifier.size(172.dp)) {
        HoldButton(
            modifier = Modifier.align(Alignment.TopCenter).size(buttonSize),
            icon = Icons.Rounded.KeyboardArrowUp,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.UP,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.BottomCenter).size(buttonSize),
            icon = Icons.Rounded.KeyboardArrowDown,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.DOWN,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.CenterStart).size(buttonSize),
            icon = Icons.Rounded.KeyboardArrowLeft,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.LEFT,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.CenterEnd).size(buttonSize),
            icon = Icons.Rounded.KeyboardArrowRight,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.RIGHT,
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f * alpha)),
        )
    }
}

@Composable
private fun ActionCluster(
    onKey: (Int, Int) -> Unit,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(180.dp)) {
        HoldButton(
            modifier = Modifier.align(Alignment.TopCenter).size(62.dp),
            label = "X",
            tint = Color(0xFF6FB6FF),
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.X,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.BottomCenter).size(62.dp),
            label = "B",
            tint = Color(0xFFFF7597),
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.B,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.CenterStart).size(62.dp),
            label = "Y",
            tint = Color(0xFFFFD166),
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.Y,
        )
        HoldButton(
            modifier = Modifier.align(Alignment.CenterEnd).size(62.dp),
            label = "A",
            tint = Color(0xFF5BE3A6),
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.A,
        )
    }
}

@Composable
private fun StartSelect(
    onKey: (Int, Int) -> Unit,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        HoldButton(
            modifier = Modifier.width(74.dp).height(32.dp),
            shape = RoundedCornerShape(50),
            label = "SELECT",
            fontSize = 9.sp,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.SELECT,
        )
        Box(Modifier.width(12.dp))
        HoldButton(
            modifier = Modifier.width(74.dp).height(32.dp),
            shape = RoundedCornerShape(50),
            label = "START",
            fontSize = 9.sp,
            alpha = alpha,
            onKey = onKey,
            keyCode = RetroKeys.START,
        )
    }
}

@Composable
private fun HoldButton(
    keyCode: Int,
    onKey: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    label: String? = null,
    icon: ImageVector? = null,
    tint: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 17.sp,
    alpha: Float = 0.9f,
) {
    var pressed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (pressed) {
                    tint.copy(alpha = 0.45f * alpha)
                } else {
                    Color.Black.copy(alpha = 0.28f * alpha)
                },
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f * alpha), shape)
            .pointerInput(keyCode) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKey(KeyEvent.ACTION_DOWN, keyCode)
                    waitForUpOrCancellation()
                    pressed = false
                    onKey(KeyEvent.ACTION_UP, keyCode)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            label != null -> Text(
                text = label,
                color = if (pressed) Color.White else tint,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )

            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun VirtualStick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var knob by remember { mutableStateOf(Offset.Zero) }
    var center by remember { mutableStateOf(Offset.Zero) }
    var maxRadius by remember { mutableStateOf(1f) }
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(138.dp)
            .onSizeChanged { size ->
                center = Offset(size.width / 2f, size.height / 2f)
                maxRadius = size.width / 2f
            }
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { position -> knob = limit(position - center, maxRadius) },
                    onDrag = { change, _ ->
                        change.consume()
                        val limited = limit(change.position - center, maxRadius)
                        knob = limited
                        onMove(
                            (limited.x / (maxRadius * 0.72f)).coerceIn(-1f, 1f),
                            (limited.y / (maxRadius * 0.72f)).coerceIn(-1f, 1f),
                        )
                    },
                    onDragEnd = {
                        knob = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knob = Offset.Zero
                        onMove(0f, 0f)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knob.x.roundToInt(), knob.y.roundToInt()) }
                .size(54.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.55f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        )
    }
}

private fun limit(delta: Offset, maxRadius: Float): Offset {
    val limit = maxRadius * 0.72f
    val distance = delta.getDistance()
    return if (distance > limit) delta * (limit / distance) else delta
}
