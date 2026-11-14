package dev.pixeldeck.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, DARK, LIGHT }

enum class ShaderChoice { DEFAULT, CRT, LCD, SHARP }

enum class ThemePreset {
    MIDNIGHT,
    OCEAN,
    SUNSET,
    EMERALD,
    VIOLET,
    ROSE,
    CYAN,
    GOLD,
    CUSTOM,
}

enum class BackgroundStyle { WAVES, AURORA, SOLID }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = false,
    val themePreset: ThemePreset = ThemePreset.MIDNIGHT,
    val accentHue: Float = 205f,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.WAVES,
    val waveIntensity: Float = 0.7f,
    val waveSpeed: Float = 1f,
    val showCategoryLabels: Boolean = true,
    val compactList: Boolean = false,
    val glowEnabled: Boolean = true,
    val showClock: Boolean = true,
    val shader: ShaderChoice = ShaderChoice.DEFAULT,
    val showFps: Boolean = false,
    val autoSaveState: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val touchControlsOpacity: Float = 0.9f,
    val confirmOnExit: Boolean = false,
)

class SettingsRepository(private val context: Context) {

    private val store = context.settingsDataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            themeMode = prefs[THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            dynamicColor = prefs[DYNAMIC_COLOR] ?: false,
            themePreset = prefs[PRESET]?.let { runCatching { ThemePreset.valueOf(it) }.getOrNull() }
                ?: ThemePreset.MIDNIGHT,
            accentHue = prefs[ACCENT_HUE] ?: 205f,
            backgroundStyle = prefs[BACKGROUND]?.let {
                runCatching { BackgroundStyle.valueOf(it) }.getOrNull()
            } ?: BackgroundStyle.WAVES,
            waveIntensity = prefs[WAVE_INTENSITY] ?: 0.7f,
            waveSpeed = prefs[WAVE_SPEED] ?: 1f,
            showCategoryLabels = prefs[CATEGORY_LABELS] ?: true,
            compactList = prefs[COMPACT_LIST] ?: false,
            glowEnabled = prefs[GLOW] ?: true,
            showClock = prefs[SHOW_CLOCK] ?: true,
            shader = prefs[SHADER]?.let { runCatching { ShaderChoice.valueOf(it) }.getOrNull() }
                ?: ShaderChoice.DEFAULT,
            showFps = prefs[SHOW_FPS] ?: false,
            autoSaveState = prefs[AUTO_SAVE] ?: true,
            hapticsEnabled = prefs[HAPTICS] ?: true,
            touchControlsOpacity = prefs[CONTROLS_OPACITY] ?: 0.9f,
            confirmOnExit = prefs[CONFIRM_EXIT] ?: false,
        )
    }

    suspend fun setTheme(mode: ThemeMode) = store.edit { it[THEME] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = store.edit { it[DYNAMIC_COLOR] = enabled }

    suspend fun setThemePreset(preset: ThemePreset) = store.edit { it[PRESET] = preset.name }

    suspend fun setAccentHue(hue: Float) = store.edit { it[ACCENT_HUE] = hue }

    suspend fun setBackgroundStyle(style: BackgroundStyle) = store.edit { it[BACKGROUND] = style.name }

    suspend fun setWaveIntensity(value: Float) = store.edit { it[WAVE_INTENSITY] = value }

    suspend fun setWaveSpeed(value: Float) = store.edit { it[WAVE_SPEED] = value }

    suspend fun setShowCategoryLabels(enabled: Boolean) = store.edit { it[CATEGORY_LABELS] = enabled }

    suspend fun setCompactList(enabled: Boolean) = store.edit { it[COMPACT_LIST] = enabled }

    suspend fun setGlow(enabled: Boolean) = store.edit { it[GLOW] = enabled }

    suspend fun setShowClock(enabled: Boolean) = store.edit { it[SHOW_CLOCK] = enabled }

    suspend fun setShader(shader: ShaderChoice) = store.edit { it[SHADER] = shader.name }

    suspend fun setShowFps(enabled: Boolean) = store.edit { it[SHOW_FPS] = enabled }

    suspend fun setAutoSaveState(enabled: Boolean) = store.edit { it[AUTO_SAVE] = enabled }

    suspend fun setHaptics(enabled: Boolean) = store.edit { it[HAPTICS] = enabled }

    suspend fun setTouchControlsOpacity(value: Float) = store.edit { it[CONTROLS_OPACITY] = value }

    suspend fun setConfirmOnExit(enabled: Boolean) = store.edit { it[CONFIRM_EXIT] = enabled }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PRESET = stringPreferencesKey("theme_preset")
        val ACCENT_HUE = floatPreferencesKey("accent_hue")
        val BACKGROUND = stringPreferencesKey("background_style")
        val WAVE_INTENSITY = floatPreferencesKey("wave_intensity")
        val WAVE_SPEED = floatPreferencesKey("wave_speed")
        val CATEGORY_LABELS = booleanPreferencesKey("category_labels")
        val COMPACT_LIST = booleanPreferencesKey("compact_list")
        val GLOW = booleanPreferencesKey("glow")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHADER = stringPreferencesKey("shader")
        val SHOW_FPS = booleanPreferencesKey("show_fps")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val HAPTICS = booleanPreferencesKey("haptics")
        val CONTROLS_OPACITY = floatPreferencesKey("controls_opacity")
        val CONFIRM_EXIT = booleanPreferencesKey("confirm_exit")
    }
}
