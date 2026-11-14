package dev.pixeldeck.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.model.SystemCatalog
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.BackgroundStyle
import dev.pixeldeck.design.component.GlassPanel
import dev.pixeldeck.design.component.XmbBackground
import dev.pixeldeck.design.component.XmbBackgroundStyle
import dev.pixeldeck.ui.common.accentColor
import dev.pixeldeck.ui.common.formatBytes
import dev.pixeldeck.ui.common.formatPlayTime
import dev.pixeldeck.ui.components.GameArtwork
import dev.pixeldeck.ui.theme.palette

@Composable
fun GameDetailScreen(
    container: AppContainer,
    gameId: Long,
    settings: AppSettings,
    onBack: () -> Unit,
    onPlay: (Long) -> Unit,
) {
    val viewModel: GameDetailViewModel = viewModel(
        key = "detail-$gameId",
        factory = viewModelFactory {
            initializer { GameDetailViewModel(container.games, gameId) }
        },
    )

    val game by viewModel.game.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    var showSystemDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) {
        if (deleted) onBack()
    }

    val current = game ?: return

    val palette = settings.palette()
    val backgroundStyle = when (settings.backgroundStyle) {
        BackgroundStyle.WAVES -> XmbBackgroundStyle.WAVES
        BackgroundStyle.AURORA -> XmbBackgroundStyle.AURORA
        BackgroundStyle.SOLID -> XmbBackgroundStyle.SOLID
    }

    XmbBackground(
        modifier = Modifier.fillMaxSize(),
        accent = palette.accent,
        secondary = palette.secondary,
        style = backgroundStyle,
        intensity = settings.waveIntensity,
        speed = settings.waveSpeed,
        glow = settings.glowEnabled,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            item {
                DetailHeader(
                    game = current,
                    onBack = onBack,
                    onToggleFavorite = viewModel::toggleFavorite,
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${current.system.displayName} · ${current.system.year}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(20.dp))

                    MetaRow(game = current)

                    TextButton(onClick = { showSystemDialog = true }) {
                        Icon(
                            Icons.Rounded.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Wrong system? Change it")
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = { onPlay(current.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = current.system.accentColor(),
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

                        FilledTonalIconButton(
                            onClick = viewModel::toggleFavorite,
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ) {
                            Icon(
                                imageVector = if (current.favorite) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = if (current.favorite) {
                                    "Remove favorite"
                                } else {
                                    "Add favorite"
                                },
                                tint = if (current.favorite) {
                                    Color(0xFFFF7597)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    InfoCard(game = current)

                    Spacer(Modifier.height(28.dp))

                    TextButton(
                        onClick = { viewModel.deleteFromLibrary(onDone = onBack) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Remove from library")
                    }
                }
            }
        }
    }

    if (showSystemDialog) {
        AlertDialog(
            onDismissRequest = { showSystemDialog = false },
            title = { Text("Choose system") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    SystemCatalog.all.sortedBy { it.displayName }.forEach { system ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSystem(system.id)
                                    showSystemDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = current.system.id == system.id,
                                onClick = {
                                    viewModel.setSystem(system.id)
                                    showSystemDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = system.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = system.shortName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSystemDialog = false }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun DetailHeader(
    game: Game,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        GameArtwork(
            game = game,
            modifier = Modifier.fillMaxSize(),
            corner = 0.dp,
            overlayTitle = false,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Transparent,
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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

            FilledTonalIconButton(
                onClick = onToggleFavorite,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.35f),
                    contentColor = if (game.favorite) Color(0xFFFF7597) else Color.White,
                ),
            ) {
                Icon(
                    imageVector = if (game.favorite) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                )
            }
        }
    }
}

@Composable
private fun MetaRow(game: Game) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetaPill(text = game.system.shortName, accent = game.system.accentColor())
        MetaPill(text = formatBytes(game.sizeBytes))
        MetaPill(text = formatPlayTime(game.playTimeMillis))
    }
}

@Composable
private fun MetaPill(text: String, accent: Color? = null) {
    GlassPanel(
        shape = RoundedCornerShape(50),
        tint = accent?.copy(alpha = 0.18f) ?: MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InfoCard(game: Game) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(game.system.accentColor().copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = game.system.shortName.take(2),
                        style = MaterialTheme.typography.labelMedium,
                        color = game.system.accentColor(),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Runs on ${game.system.core.name}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Core downloads automatically on first play",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (game.system.notes != null || game.system.experimental) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = game.system.notes
                            ?: "This system is experimental. Compatibility and speed will vary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = game.relativePath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Start,
            )
        }
    }
}
