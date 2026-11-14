package dev.pixeldeck.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.settings.BackgroundStyle
import dev.pixeldeck.data.settings.ShaderChoice
import dev.pixeldeck.data.settings.ThemeMode
import dev.pixeldeck.data.settings.ThemePreset
import dev.pixeldeck.design.component.GlassPanel
import dev.pixeldeck.design.component.XmbBackground
import dev.pixeldeck.design.component.XmbBackgroundStyle
import dev.pixeldeck.ui.theme.accentFor
import dev.pixeldeck.ui.theme.label
import dev.pixeldeck.ui.theme.palette
import java.util.Locale

@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(container) }
        },
    )

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val palette = settings.palette()
    val backgroundStyle = when (settings.backgroundStyle) {
        BackgroundStyle.WAVES -> XmbBackgroundStyle.WAVES
        BackgroundStyle.AURORA -> XmbBackgroundStyle.AURORA
        BackgroundStyle.SOLID -> XmbBackgroundStyle.SOLID
    }
    val biosInstalled by viewModel.biosInstalled.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val biosPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) viewModel.importBios(uri)
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
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
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Tune your deck",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Appearance") {
                    SwitchRow(
                        icon = Icons.Rounded.Palette,
                        title = "Dynamic color",
                        subtitle = "Use wallpaper-derived surfaces on Android 12+",
                        checked = settings.dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setTheme(mode) },
                                label = { Text(mode.label()) },
                                colors = settingsChipColors(),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Colour theme",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = settings.themePreset == preset,
                                onClick = { viewModel.setThemePreset(preset) },
                                label = { Text(preset.label()) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(accentFor(preset, settings.accentHue))
                                            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                                    )
                                },
                                colors = settingsChipColors(),
                            )
                        }
                    }

                    if (settings.themePreset == ThemePreset.CUSTOM) {
                        Spacer(Modifier.height(10.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Custom hue",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = "${settings.accentHue.toInt()}\u00B0",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentFor(ThemePreset.CUSTOM, settings.accentHue),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFF5252),
                                                Color(0xFFFFD740),
                                                Color(0xFF69F0AE),
                                                Color(0xFF40C4FF),
                                                Color(0xFF7C4DFF),
                                                Color(0xFFFF4081),
                                                Color(0xFFFF5252),
                                            ),
                                        ),
                                    ),
                            )
                            Slider(
                                value = settings.accentHue,
                                onValueChange = viewModel::setAccentHue,
                                valueRange = 0f..360f,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                SectionCard(title = "XMB look") {
                    Text(
                        text = "Background",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BackgroundStyle.entries.forEach { style ->
                            FilterChip(
                                selected = settings.backgroundStyle == style,
                                onClick = { viewModel.setBackgroundStyle(style) },
                                label = { Text(style.label()) },
                                colors = settingsChipColors(),
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    SliderRow(
                        title = "Wave intensity",
                        value = settings.waveIntensity,
                        valueRange = 0f..1f,
                        valueLabel = "${(settings.waveIntensity * 100).toInt()}%",
                        onValueChange = viewModel::setWaveIntensity,
                    )
                    SliderRow(
                        title = "Wave speed",
                        value = settings.waveSpeed,
                        valueRange = 0.25f..2.5f,
                        valueLabel = String.format(Locale.US, "%.2fx", settings.waveSpeed),
                        onValueChange = viewModel::setWaveSpeed,
                    )

                    SwitchRow(
                        icon = Icons.Rounded.Schedule,
                        title = "Show clock",
                        subtitle = "Display the time in the XMB header",
                        checked = settings.showClock,
                        onCheckedChange = viewModel::setShowClock,
                    )
                    SwitchRow(
                        icon = Icons.Rounded.Label,
                        title = "Category labels",
                        subtitle = "Show text under the XMB icons",
                        checked = settings.showCategoryLabels,
                        onCheckedChange = viewModel::setShowCategoryLabels,
                    )
                    SwitchRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Icon glow",
                        subtitle = "Soft light around the selected icon",
                        checked = settings.glowEnabled,
                        onCheckedChange = viewModel::setGlow,
                    )
                    SwitchRow(
                        icon = Icons.Rounded.Tune,
                        title = "Compact list",
                        subtitle = "Fit more games on screen",
                        checked = settings.compactList,
                        onCheckedChange = viewModel::setCompactList,
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                SectionCard(title = "Gameplay") {
                    Text(
                        text = "Default shader",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShaderChoice.entries.forEach { shader ->
                            FilterChip(
                                selected = settings.shader == shader,
                                onClick = { viewModel.setShader(shader) },
                                label = { Text(shader.label()) },
                                colors = settingsChipColors(),
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    SwitchRow(
                        icon = Icons.Rounded.Save,
                        title = "Auto-save state",
                        subtitle = "Resume exactly where you left off",
                        checked = settings.autoSaveState,
                        onCheckedChange = viewModel::setAutoSave,
                    )
                    SwitchRow(
                        icon = Icons.Rounded.Speed,
                        title = "Show FPS",
                        subtitle = "Display frame rate while playing",
                        checked = settings.showFps,
                        onCheckedChange = viewModel::setShowFps,
                    )
                    SwitchRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Haptics",
                        subtitle = "Feel the on-screen controls",
                        checked = settings.hapticsEnabled,
                        onCheckedChange = viewModel::setHaptics,
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Control opacity",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${(settings.touchControlsOpacity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Slider(
                            value = settings.touchControlsOpacity,
                            onValueChange = viewModel::setControlsOpacity,
                            valueRange = 0.25f..1f,
                        )
                    }
                }
            }

            item {
                SectionCard(title = "System files") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = if (biosInstalled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PlayStation BIOS",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = if (biosInstalled) "Installed" else "Not installed",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (biosInstalled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        TextButton(onClick = { biosPicker.launch(arrayOf("*/*")) }) {
                            Text(if (biosInstalled) "Replace" else "Import")
                        }
                    }
                    Text(
                        text = "Rename your BIOS dump to scph5500.bin, scph5501.bin, or scph5502.bin. " +
                            "PixelDeck never ships BIOS files.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            item {
                SectionCard(title = "Library") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connected folders",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${viewModel.folderCount} folder(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearDialog = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Clear library",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "Removes games from PixelDeck, not your files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "About") {
                    InfoRow(
                        icon = Icons.Rounded.Tune,
                        title = "PixelDeck 1.0.0",
                        subtitle = "A libretro frontend for Android",
                    )
                    InfoRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Powered by libretro",
                        subtitle = "Cores are downloaded from the libretro buildbot",
                    )
                    Text(
                        text = "PixelDeck ships no ROMs, BIOS files, or keys. " +
                            "Provide your own legally obtained content. " +
                            "Emulator cores are licensed individually, mostly GPLv3.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear the library?") },
            text = {
                Text("Games will be removed from PixelDeck. Your ROM files stay untouched.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearLibrary(onDone = onBack)
                    },
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
        )
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp), content = content)
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun settingsChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
)

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.DARK -> "Dark"
    ThemeMode.LIGHT -> "Light"
}

private fun ShaderChoice.label(): String = when (this) {
    ShaderChoice.DEFAULT -> "Default"
    ShaderChoice.CRT -> "CRT"
    ShaderChoice.LCD -> "LCD"
    ShaderChoice.SHARP -> "Sharp"
}

private fun BackgroundStyle.label(): String = when (this) {
    BackgroundStyle.WAVES -> "Waves"
    BackgroundStyle.AURORA -> "Aurora"
    BackgroundStyle.SOLID -> "Solid"
}
