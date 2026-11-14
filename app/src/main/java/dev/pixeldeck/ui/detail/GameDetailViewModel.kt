package dev.pixeldeck.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.repo.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val repository: GameRepository,
    private val gameId: Long,
) : ViewModel() {

    val game: StateFlow<Game?> = repository.game(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted

    fun toggleFavorite() {
        val current = game.value ?: return
        viewModelScope.launch {
            repository.setFavorite(current.id, !current.favorite)
        }
    }

    fun deleteFromLibrary(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(gameId)
            _deleted.value = true
            onDone()
        }
    }
}
