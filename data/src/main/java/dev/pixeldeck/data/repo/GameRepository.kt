package dev.pixeldeck.data.repo

import android.net.Uri
import dev.pixeldeck.data.db.GameDao
import dev.pixeldeck.data.db.GameEntity
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.model.SystemCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ScannedGame(
    val title: String,
    val fileName: String,
    val uri: Uri,
    val treeUri: Uri,
    val relativePath: String,
    val systemId: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val discUris: List<Uri> = emptyList(),
)

data class ScanResult(
    val imported: Int,
    val updated: Int,
    val skipped: Int,
    val message: String? = null,
)

class GameRepository(private val dao: GameDao) {

    val games: Flow<List<Game>> = dao.observeAll().map { list -> list.mapNotNull(::toDomain) }

    fun game(id: Long): Flow<Game?> = dao.observe(id).map { it?.let(::toDomain) }

    suspend fun get(id: Long): Game? = dao.get(id)?.let(::toDomain)

    suspend fun importScanned(scanned: List<ScannedGame>): ScanResult {
        val existing = dao.getAllOnce().associateBy { it.uri }
        val now = System.currentTimeMillis()
        var imported = 0
        var updated = 0
        val toInsert = mutableListOf<GameEntity>()

        for (item in scanned) {
            val discUris = item.discUris.joinToString("\n") { it.toString() }
            val old = existing[item.uri.toString()]
            if (old == null) {
                toInsert += GameEntity(
                    title = item.title,
                    fileName = item.fileName,
                    uri = item.uri.toString(),
                    treeUri = item.treeUri.toString(),
                    relativePath = item.relativePath,
                    systemId = item.systemId,
                    sizeBytes = item.sizeBytes,
                    lastModified = item.lastModified,
                    addedAt = now,
                    discUris = discUris,
                )
                imported++
            } else {
                dao.update(
                    old.copy(
                        title = item.title,
                        fileName = item.fileName,
                        treeUri = item.treeUri.toString(),
                        relativePath = item.relativePath,
                        systemId = item.systemId,
                        sizeBytes = item.sizeBytes,
                        lastModified = item.lastModified,
                        discUris = discUris,
                    ),
                )
                updated++
            }
        }

        if (toInsert.isNotEmpty()) dao.insertAll(toInsert)

        return ScanResult(imported = imported, updated = updated, skipped = 0)
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun markPlayed(id: Long, time: Long = System.currentTimeMillis()) = dao.markPlayed(id, time)

    suspend fun addPlayTime(id: Long, delta: Long) = dao.addPlayTime(id, delta)

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun clear() = dao.clear()

    private fun toDomain(entity: GameEntity): Game? {
        val system = SystemCatalog.byId[entity.systemId] ?: return null
        return Game(
            id = entity.id,
            title = entity.title,
            fileName = entity.fileName,
            uri = Uri.parse(entity.uri),
            treeUri = Uri.parse(entity.treeUri),
            relativePath = entity.relativePath,
            system = system,
            sizeBytes = entity.sizeBytes,
            lastModified = entity.lastModified,
            addedAt = entity.addedAt,
            lastPlayedAt = entity.lastPlayedAt,
            playTimeMillis = entity.playTimeMillis,
            favorite = entity.favorite,
            discUris = entity.discUris.lineSequence()
                .filter { it.isNotBlank() }
                .map(Uri::parse)
                .toList(),
        )
    }
}
