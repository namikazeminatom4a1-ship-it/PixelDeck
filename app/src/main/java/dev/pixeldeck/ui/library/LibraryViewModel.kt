package dev.pixeldeck.ui.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.model.GameSystem
import dev.pixeldeck.data.repo.FolderStore
import dev.pixeldeck.data.repo.GameRepository
import dev.pixeldeck.data.repo.RomScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object LibraryCategories {
    const val ALL = "all"
    const val RECENT = "recent"
    const val FAVORITES = "favorites"
    const val SYSTEM_PREFIX = "system:"

    fun system(id: String): String = SYSTEM_PREFIX + id
}

data class LibraryUiState(
    val allGames: List<Game> = emptyList(),
    val games: List<Game> = emptyList(),
    val systems: List<GameSystem> = emptyList(),
    val query: String = "",
    val selectedCategory: String = LibraryCategories.ALL,
    val isScanning: Boolean = false,
    val message: String? = null,
) {
    val isEmpty: Boolean get() = allGames.isEmpty()
    val isFilteredEmpty: Boolean get() = !isEmpty && games.isEmpty()
    val selectedSystemId: String?
        get() = selectedCategory.takeIf { it.startsWith(LibraryCategories.SYSTEM_PREFIX) }
            ?.removePrefix(LibraryCategories.SYSTEM_PREFIX)
}

private data class Filters(
    val query: String,
    val category: String,
)

private data class Status(
    val scanning: Boolean,
    val message: String?,
)

@SuppressLint("StaticFieldLeak")
class LibraryViewModel(
    private val context: Context,
    private val repository: GameRepository,
    private val scanner: RomScanner,
    private val folders: FolderStore,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(LibraryCategories.ALL)
    private val scanning = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    private val filters = combine(query, selectedCategory) { q, c -> Filters(q, c) }

    private val status = combine(scanning, message) { s, m -> Status(s, m) }

    val state: StateFlow<LibraryUiState> =
        combine(repository.games, filters, status) { games, filter, stat ->
            val systemId = filter.category
                .takeIf { it.startsWith(LibraryCategories.SYSTEM_PREFIX) }
                ?.removePrefix(LibraryCategories.SYSTEM_PREFIX)

            val visible = games.filter { game ->
                val matchesCategory = when {
                    filter.category == LibraryCategories.FAVORITES -> game.favorite
                    filter.category == LibraryCategories.RECENT -> game.lastPlayedAt != null
                    systemId != null -> game.system.id == systemId
                    else -> true
                }
                matchesCategory &&
                    (filter.query.isBlank() || game.title.contains(filter.query, ignoreCase = true))
            }

            val ordered = if (filter.category == LibraryCategories.RECENT) {
                visible.sortedByDescending { it.lastPlayedAt ?: 0L }
            } else {
                visible
            }

            LibraryUiState(
                allGames = games,
                games = ordered,
                systems = games.map { it.system }.distinctBy { it.id }
                    .sortedBy { it.displayName },
                query = filter.query,
                selectedCategory = filter.category,
                isScanning = stat.scanning,
                message = stat.message,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )

    fun setQuery(value: String) {
        query.value = value
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun consumeMessage() {
        message.value = null
    }

    fun scanFolder(uri: Uri) {
        viewModelScope.launch {
            scanning.value = true
            try {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                folders.add(uri)
                val result = scanner.scan(uri)
                message.value = buildString {
                    append("Added ${result.imported} game")
                    if (result.imported != 1) append('s')
                    if (result.updated > 0) append(", refreshed ${result.updated}")
                }
            } catch (e: Exception) {
                message.value = e.message ?: "Could not read that folder."
            } finally {
                scanning.value = false
            }
        }
    }

    fun rescanAll() {
        if (scanning.value) return
        val trees = folders.all()
        if (trees.isEmpty()) {
            message.value = "No folders added yet."
            return
        }
        viewModelScope.launch {
            scanning.value = true
            var imported = 0
            var updated = 0
            try {
                trees.forEach { tree ->
                    val result = runCatching { scanner.scan(tree) }.getOrNull()
                    if (result != null) {
                        imported += result.imported
                        updated += result.updated
                    }
                }
                message.value = if (imported + updated == 0) {
                    "Everything is up to date."
                } else {
                    "Added $imported, refreshed $updated."
                }
            } finally {
                scanning.value = false
            }
        }
    }
}
