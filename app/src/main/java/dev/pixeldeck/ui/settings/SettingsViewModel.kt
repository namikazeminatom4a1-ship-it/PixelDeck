package dev.pixeldeck.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.BackgroundStyle
import dev.pixeldeck.data.settings.ShaderChoice
import dev.pixeldeck.data.settings.ThemeMode
import dev.pixeldeck.data.settings.ThemePreset
import dev.pixeldeck.emulation.EmulationSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<AppSettings> = container.settings.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    val folderCount: Int get() = container.folders.all().size

    private val _biosInstalled = MutableStateFlow(container.saves.hasBios(*EmulationSession.PS1_BIOS_NAMES))
    val biosInstalled: StateFlow<Boolean> = _biosInstalled.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        container.settings.setTheme(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        container.settings.setDynamicColor(enabled)
    }

    fun setThemePreset(preset: ThemePreset) = viewModelScope.launch {
        container.settings.setThemePreset(preset)
    }

    fun setAccentHue(value: Float) = viewModelScope.launch {
        container.settings.setAccentHue(value)
    }

    fun setBackgroundStyle(style: BackgroundStyle) = viewModelScope.launch {
        container.settings.setBackgroundStyle(style)
    }

    fun setWaveIntensity(value: Float) = viewModelScope.launch {
        container.settings.setWaveIntensity(value)
    }

    fun setWaveSpeed(value: Float) = viewModelScope.launch {
        container.settings.setWaveSpeed(value)
    }

    fun setShowCategoryLabels(enabled: Boolean) = viewModelScope.launch {
        container.settings.setShowCategoryLabels(enabled)
    }

    fun setCompactList(enabled: Boolean) = viewModelScope.launch {
        container.settings.setCompactList(enabled)
    }

    fun setGlow(enabled: Boolean) = viewModelScope.launch {
        container.settings.setGlow(enabled)
    }

    fun setShowClock(enabled: Boolean) = viewModelScope.launch {
        container.settings.setShowClock(enabled)
    }

    fun setShader(shader: ShaderChoice) = viewModelScope.launch {
        container.settings.setShader(shader)
    }

    fun setShowFps(enabled: Boolean) = viewModelScope.launch {
        container.settings.setShowFps(enabled)
    }

    fun setAutoSave(enabled: Boolean) = viewModelScope.launch {
        container.settings.setAutoSaveState(enabled)
    }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch {
        container.settings.setHaptics(enabled)
    }

    fun setControlsOpacity(value: Float) = viewModelScope.launch {
        container.settings.setTouchControlsOpacity(value)
    }

    fun importBios(uri: Uri) = viewModelScope.launch {
        runCatching { container.session.importSystemFile(uri) }
            .onSuccess { file ->
                _biosInstalled.value = container.saves.hasBios(*EmulationSession.PS1_BIOS_NAMES)
                _message.value = "Installed ${file.name}"
            }
            .onFailure { _message.value = it.message ?: "Could not install that file." }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun clearLibrary(onDone: () -> Unit) = viewModelScope.launch {
        container.games.clear()
        onDone()
    }
}
