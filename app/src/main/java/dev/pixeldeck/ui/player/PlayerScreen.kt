package dev.pixeldeck.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.ShutterSpeed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.LibretroDroid
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.ShaderChoice
import dev.pixeldeck.design.component.GlassPanel
import dev.pixeldeck.emulation.PreparedGame
import dev.pixeldeck.emulation.Shaders
import dev.pixeldeck.ui.common.accentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun PlayerScreen(
    container: AppContainer,
    gameId: Long,
    settings: AppSettings,
    onBack: () -> Unit,
) {
    val viewModel: PlayerViewModel = viewModel(
        key = "player-$gameId",
        factory = viewModelFactory {
            initializer { PlayerViewModel(container, gameId) }
        },
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(gameId) { viewModel.load() }
    PlayerImmersiveEffect()

    val game = state.game
    when {
        state.phase == PlayerPhase.ERROR -> PlayerErrorScreen(
            title = game?.title ?: "Game",
            message = state.error ?: "Something went wrong.",
            onRetry = viewModel::retry,
            onBack = onBack,
        )

        game == null || state.prepared == null -> PlayerPreparingScreen(
            game = game,
            progress = state.coreProgress,
            onBack = onBack,
        )

        else -> PlayerContent(
            container = container,
            viewModel = viewModel,
            game = game,
            prepared = state.prepared!!,
            settings = settings,
            onBack = onBack,
        )
    }
}

@Composable
private fun PlayerImmersiveEffect() {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    DisposableEffect(activity) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun PlayerContent(
    container: AppContainer,
    viewModel: PlayerViewModel,
    game: Game,
    prepared: PreparedGame,
    settings: AppSettings,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val data = remember(prepared) {
        GLRetroViewData(context).apply {
            coreFilePath = prepared.corePath
            gameFilePath = prepared.gamePath
            gameVirtualFiles = prepared.virtualFiles
            systemDirectory = prepared.systemDir
            savesDirectory = prepared.savesDir
            shader = Shaders.config(settings.shader)
            preferLowLatencyAudio = true
            rumbleEventsEnabled = true
            saveRAMState = container.saves.loadSram("game-${game.id}")
        }
    }

    val view = remember(prepared) { GLRetroView(context, data) }

    var controlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }
    var menuVisible by remember { mutableStateOf(false) }
    var fastForward by remember { mutableStateOf(false) }
    var fps by remember { mutableIntStateOf(0) }
    var fatalError by remember { mutableStateOf<String?>(null) }
    var shader by remember { mutableStateOf(settings.shader) }
    val exitGuard = remember { AtomicBoolean(false) }
    val viewCreated = remember { AtomicBoolean(false) }

    fun exit() {
        if (!exitGuard.compareAndSet(false, true)) return
        scope.launch {
            viewModel.persist(view, settings.autoSaveState)
            onBack()
        }
    }

    BackHandler { exit() }

    LaunchedEffect(controlsVisible, interactionTick, menuVisible) {
        if (controlsVisible && !menuVisible) {
            delay(4_000)
            controlsVisible = false
        }
    }

    LaunchedEffect(view) {
        var frames = 0
        var last = System.currentTimeMillis()
        var restored = false

        view.getGLRetroEvents().collect { event ->
            when (event) {
                GLRetroView.GLRetroEvents.FrameRendered -> {
                    frames++
                    val now = System.currentTimeMillis()
                    if (now - last >= 1_000) {
                        fps = frames
                        frames = 0
                        last = now
                    }
                }

                GLRetroView.GLRetroEvents.SurfaceCreated -> {
                    if (!restored && settings.autoSaveState) {
                        restored = true
                        val bytes = viewModel.loadStateBytes()
                        if (bytes != null) {
                            withContext(Dispatchers.IO) {
                                runCatching { view.unserializeState(bytes) }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(view) {
        view.getGLRetroErrors().collect { code ->
            fatalError = errorMessage(code)
        }
    }

    DisposableEffect(view) {
        onDispose {
            runCatching { view.onDestroy() }
            viewModel.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = {
                view.also {
                    if (viewCreated.compareAndSet(false, true)) {
                        it.onCreate(lifecycleOwner)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        controlsVisible = !controlsVisible
                        interactionTick++
                    }
                },
        )

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(220)),
        ) {
            ControllerOverlay(
                view = view,
                game = game,
                settings = settings,
                onInteraction = { interactionTick++ },
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(150)) + slideInVertically(tween(180)) { -it / 3 },
            exit = fadeOut(tween(150)),
        ) {
            PlayerTopBar(
                game = game,
                fps = fps,
                showFps = settings.showFps,
                onBack = { exit() },
                onMenu = { menuVisible = true },
            )
        }

        if (settings.showFps && !controlsVisible) {
            GlassPanel(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "$fps FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = fpsColor(fps),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }

        fatalError?.let { message ->
            FatalErrorOverlay(
                message = message,
                onDismiss = { fatalError = null },
                onExit = { exit() },
            )
        }
    }

    if (menuVisible) {
        PlayerMenuSheet(
            hasSaveState = viewModel.hasSaveState(),
            fastForward = fastForward,
            shader = shader,
            onDismiss = { menuVisible = false },
            onSaveState = {
                menuVisible = false
                scope.launch {
                    val ok = viewModel.saveStateNow(view)
                    toast(context, if (ok) "State saved" else "Couldn't save state")
                }
            },
            onLoadState = {
                menuVisible = false
                scope.launch {
                    val ok = viewModel.loadState(view)
                    toast(context, if (ok) "State loaded" else "No save state yet")
                }
            },
            onReset = {
                menuVisible = false
                view.reset()
                toast(context, "Game reset")
            },
            onToggleFastForward = {
                fastForward = !fastForward
                view.frameSpeed = if (fastForward) 4 else 1
            },
            onCycleShader = {
                val next = ShaderChoice.entries[(shader.ordinal + 1) % ShaderChoice.entries.size]
                shader = next
                view.shader = Shaders.config(next)
                scope.launch { container.settings.setShader(next) }
            },
            onExit = {
                menuVisible = false
                exit()
            },
        )
    }
}

@Composable
private fun PlayerTopBar(
    game: Game,
    fps: Int,
    showFps: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.7f),
                    1f to Color.Transparent,
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.35f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = game.system.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (showFps) {
                GlassPanel(shape = RoundedCornerShape(50)) {
                    Text(
                        text = "$fps FPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = fpsColor(fps),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            FilledTonalIconButton(
                onClick = onMenu,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.35f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerMenuSheet(
    hasSaveState: Boolean,
    fastForward: Boolean,
    shader: ShaderChoice,
    onDismiss: () -> Unit,
    onSaveState: () -> Unit,
    onLoadState: () -> Unit,
    onReset: () -> Unit,
    onToggleFastForward: () -> Unit,
    onCycleShader: () -> Unit,
    onExit: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = "Session",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(6.dp))

            MenuRow(
                icon = Icons.Rounded.Save,
                title = "Save state",
                subtitle = "Snapshot the action right now",
                onClick = onSaveState,
            )
            MenuRow(
                icon = Icons.Rounded.SettingsBackupRestore,
                title = "Load state",
                subtitle = if (hasSaveState) "Restore your last snapshot" else "No snapshot yet",
                enabled = hasSaveState,
                onClick = onLoadState,
            )
            MenuRow(
                icon = Icons.Rounded.FastForward,
                title = "Fast forward",
                subtitle = if (fastForward) "4x speed · on" else "4x speed · off",
                onClick = onToggleFastForward,
            )
            MenuRow(
                icon = Icons.Rounded.ShutterSpeed,
                title = "Shader",
                subtitle = shaderLabel(shader),
                onClick = onCycleShader,
            )
            MenuRow(
                icon = Icons.Rounded.Refresh,
                title = "Reset game",
                subtitle = "Restart from the beginning",
                onClick = onReset,
            )
            MenuRow(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                title = "Exit",
                subtitle = "Saves your progress automatically",
                onClick = onExit,
            )
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.16f else 0.06f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.4f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (enabled) 0.8f else 0.35f,
                ),
            )
        }
    }
}

@Composable
private fun PlayerPreparingScreen(
    game: Game?,
    progress: Float,
    onBack: () -> Unit,
) {
    val accent = game?.system?.accentColor() ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = 0.28f), Color(0xFF07070B)),
                ),
            ),
    ) {
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(14.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = accent, strokeWidth = 3.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = game?.title ?: "Loading game…",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (progress > 0f && progress < 1f) {
                    "Getting the ${game?.system?.shortName ?: ""} core ready…"
                } else {
                    "Warming up the emulator…"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
            )

            if (progress > 0f && progress < 1f) {
                Spacer(Modifier.height(22.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = accent,
                    trackColor = Color.White.copy(alpha = 0.12f),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun PlayerErrorScreen(
    title: String,
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070B)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Couldn't start $title",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 13.dp),
            ) {
                Text("Try again")
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onBack) {
                Text("Back to library", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun FatalErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        GlassPanel(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "The emulator hit a snag",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss", color = Color.White.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Exit game")
                    }
                }
            }
        }
    }
}

private fun fpsColor(fps: Int): Color = when {
    fps >= 55 -> Color(0xFF5BE3A6)
    fps >= 30 -> Color(0xFFFFD166)
    else -> Color(0xFFFF7597)
}

private fun shaderLabel(shader: ShaderChoice): String = when (shader) {
    ShaderChoice.DEFAULT -> "Default · crisp"
    ShaderChoice.CRT -> "CRT · scanlines"
    ShaderChoice.LCD -> "LCD · handheld grid"
    ShaderChoice.SHARP -> "Sharp · nearest"
}

private fun errorMessage(code: Int): String = when (code) {
    LibretroDroid.ERROR_LOAD_LIBRARY -> "The emulator core could not be loaded."
    LibretroDroid.ERROR_LOAD_GAME -> "This ROM could not be read. It may be corrupt or unsupported."
    LibretroDroid.ERROR_GL_NOT_COMPATIBLE -> "This device's GPU doesn't support the required graphics."
    LibretroDroid.ERROR_SERIALIZATION -> "The save state could not be written."
    LibretroDroid.ERROR_CHEAT -> "A cheat code was rejected by the core."
    else -> "The emulator stopped unexpectedly."
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
