package dev.pixeldeck.emulation

import android.content.Context
import java.io.File

class SaveManager(context: Context) {

    val systemDir: File = File(context.filesDir, "system").apply { mkdirs() }
    val savesDir: File = File(context.filesDir, "saves").apply { mkdirs() }
    private val statesDir: File = File(context.filesDir, "states").apply { mkdirs() }

    private fun stateFile(gameId: Long) = File(statesDir, "game-$gameId.state")

    fun hasState(gameId: Long): Boolean = stateFile(gameId).isFile

    fun saveState(gameId: Long, data: ByteArray) {
        val file = stateFile(gameId)
        val temp = File(statesDir, "${file.name}.tmp")
        temp.writeBytes(data)
        if (!temp.renameTo(file)) {
            temp.copyTo(file, overwrite = true)
            temp.delete()
        }
    }

    fun loadState(gameId: Long): ByteArray? =
        stateFile(gameId).takeIf { it.isFile }?.readBytes()

    fun deleteState(gameId: Long) {
        stateFile(gameId).delete()
    }

    fun saveSram(name: String, data: ByteArray) {
        File(savesDir, "$name.srm").writeBytes(data)
    }

    fun loadSram(name: String): ByteArray? =
        File(savesDir, "$name.srm").takeIf { it.isFile }?.readBytes()

    fun hasBios(vararg names: String): Boolean {
        val installed = systemDir.listFiles()?.map { it.name.lowercase() }?.toSet() ?: emptySet()
        return names.any { it.lowercase() in installed }
    }
}
