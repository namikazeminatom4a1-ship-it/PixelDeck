package dev.pixeldeck.emulation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.swordfish.libretrodroid.VirtualFile
import dev.pixeldeck.data.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PreparedGame(
    val corePath: String,
    val gamePath: String?,
    val virtualFiles: List<VirtualFile>,
    val systemDir: String,
    val savesDir: String,
    val cacheDir: File?,
)

class EmulationSession(
    private val context: Context,
    private val coreManager: CoreManager,
    private val saveManager: SaveManager,
) {

    suspend fun prepare(
        game: Game,
        onCoreProgress: ((Float) -> Unit)? = null,
    ): PreparedGame = withContext(Dispatchers.IO) {
        val core = coreManager.ensureCore(game.system, onCoreProgress)
        val extension = game.fileName.substringAfterLast('.', "").lowercase()

        if (extension in MULTI_FILE_FORMATS) {
            prepareCopied(game, core.absolutePath)
        } else {
            prepareVirtual(game, core.absolutePath)
        }
    }

    fun cleanup(prepared: PreparedGame) {
        prepared.cacheDir?.deleteRecursively()
    }

    suspend fun importSystemFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val name = (displayName(uri) ?: uri.lastPathSegment ?: "system.bin")
            .substringAfterLast('/')
        val target = File(saveManager.systemDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: throw CoreDownloadException("Could not read that file.")
        target
    }

    private fun prepareVirtual(game: Game, corePath: String): PreparedGame {
        val descriptor = context.contentResolver.openFileDescriptor(game.uri, "r")
            ?: throw CoreDownloadException("Could not open ${game.fileName}.")
        return PreparedGame(
            corePath = corePath,
            gamePath = null,
            virtualFiles = listOf(VirtualFile(game.fileName, descriptor)),
            systemDir = saveManager.systemDir.absolutePath,
            savesDir = saveManager.savesDir.absolutePath,
            cacheDir = null,
        )
    }

    private fun prepareCopied(game: Game, corePath: String): PreparedGame {
        val dir = File(context.cacheDir, "roms/${game.id}-${System.currentTimeMillis()}")
        if (!dir.mkdirs()) throw CoreDownloadException("Could not prepare a staging folder.")

        copyTo(game.uri, File(dir, game.fileName))
        game.discUris.forEach { uri ->
            val name = displayName(uri) ?: uri.lastPathSegment ?: return@forEach
            copyTo(uri, File(dir, name))
        }

        return PreparedGame(
            corePath = corePath,
            gamePath = File(dir, game.fileName).absolutePath,
            virtualFiles = emptyList(),
            systemDir = saveManager.systemDir.absolutePath,
            savesDir = saveManager.savesDir.absolutePath,
            cacheDir = dir,
        )
    }

    private fun copyTo(uri: Uri, target: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: throw CoreDownloadException("Could not read ${target.name}.")
    }

    private fun displayName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) return cursor.getString(index)
                }
            }
        return null
    }

    companion object {
        private val MULTI_FILE_FORMATS = setOf("cue", "gdi", "m3u")

        val PS1_BIOS_NAMES = arrayOf(
            "scph5500.bin",
            "scph5501.bin",
            "scph5502.bin",
            "scph1001.bin",
            "scph7001.bin",
            "scph101.bin",
            "psxonpsp660.bin",
        )
    }
}
