package dev.pixeldeck.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swordfish.libretrodroid.GLRetroView
import dev.pixeldeck.AppContainer
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.emulation.PreparedGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PlayerPhase { PREPARING, READY, ERROR }

data class PlayerUiState(
    val game: Game? = null,
    val prepared: PreparedGame? = null,
    val phase: PlayerPhase = PlayerPhase.PREPARING,
    val coreProgress: Float = 0f,
    val error: String? = null,
    val hasSaveState: Boolean = false,
)

class PlayerViewModel(
    private val container: AppContainer,
    private val gameId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var startedAt = 0L
    @Volatile
    private var finished = false

    fun load() {
        if (_state.value.game != null) return
        viewModelScope.launch {
            val game = container.games.get(gameId)
            if (game == null) {
                _state.update { it.copy(phase = PlayerPhase.ERROR, error = "This game is no longer in your library.") }
                return@launch
            }

            _state.update {
                it.copy(game = game, hasSaveState = container.saves.hasState(gameId))
            }

            try {
                val prepared = container.session.prepare(game) { progress ->
                    _state.update { it.copy(coreProgress = progress) }
                }
                startedAt = System.currentTimeMillis()
                container.games.markPlayed(gameId)
                _state.update {
                    it.copy(prepared = prepared, phase = PlayerPhase.READY, coreProgress = 1f)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        phase = PlayerPhase.ERROR,
                        error = e.message ?: "Could not start this game.",
                    )
                }
            }
        }
    }

    fun retry() {
        _state.value = PlayerUiState()
        load()
    }

    suspend fun persist(view: GLRetroView, autoSave: Boolean) = withContext(Dispatchers.IO) {
        if (finished) return@withContext
        runCatching {
            if (autoSave) {
                val state = view.serializeState()
                if (state.isNotEmpty()) {
                    container.saves.saveState(gameId, state)
                    _state.update { it.copy(hasSaveState = true) }
                }
            }
            val sram = view.serializeSRAM()
            if (sram.isNotEmpty()) container.saves.saveSram("game-$gameId", sram)
        }
        if (startedAt > 0) {
            val delta = System.currentTimeMillis() - startedAt
            if (delta > 1_000) container.games.addPlayTime(gameId, delta)
            startedAt = System.currentTimeMillis()
        }
    }

    suspend fun saveStateNow(view: GLRetroView): Boolean = withContext(Dispatchers.IO) {
        val bytes = runCatching { view.serializeState() }.getOrNull() ?: return@withContext false
        if (bytes.isEmpty()) return@withContext false
        container.saves.saveState(gameId, bytes)
        _state.update { it.copy(hasSaveState = true) }
        true
    }

    suspend fun loadState(view: GLRetroView): Boolean = withContext(Dispatchers.IO) {
        val bytes = container.saves.loadState(gameId) ?: return@withContext false
        runCatching { view.unserializeState(bytes) }.getOrDefault(false)
    }

    fun hasSaveState(): Boolean = container.saves.hasState(gameId)

    fun loadStateBytes(): ByteArray? = container.saves.loadState(gameId)

    fun finish() {
        if (finished) return
        finished = true
        _state.value.prepared?.let { prepared ->
            prepared.virtualFiles.forEach { file ->
                runCatching { file.fileDescriptor.close() }
            }
            container.session.cleanup(prepared)
        }
    }
}
