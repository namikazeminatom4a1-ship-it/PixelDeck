package dev.pixeldeck.emulation

import android.content.Context
import android.os.Build
import dev.pixeldeck.data.model.GameSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class CoreDownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

class CoreManager(private val context: Context) {

    private val coresDir: File = File(context.filesDir, "cores").apply { mkdirs() }
    private val mutex = Mutex()

    fun installedCore(system: GameSystem): File? {
        val file = File(coresDir, system.core.soName)
        return file.takeIf { it.isFile && it.length() > 0 }
    }

    fun coreFile(system: GameSystem): File = File(coresDir, system.core.soName)

    fun downloadUrl(system: GameSystem): String =
        "https://buildbot.libretro.com/nightly/android/latest/$abi/${system.core.zipName}"

    suspend fun ensureCore(
        system: GameSystem,
        onProgress: ((Float) -> Unit)? = null,
    ): File = withContext(Dispatchers.IO) {
        installedCore(system)?.let { return@withContext it }

        mutex.withLock {
            installedCore(system)?.let { return@withContext it }
            download(system, onProgress)
        }
    }

    private fun download(system: GameSystem, onProgress: ((Float) -> Unit)?): File {
        val url = downloadUrl(system)
        val temp = File(coresDir, "${system.core.name}.download")
        val target = coreFile(system)
        val staged = File(coresDir, "${system.core.name}.staged")

        try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "PixelDeck/${BuildConfigVersion.VERSION}")
            }

            if (connection.responseCode !in 200..299) {
                throw CoreDownloadException(
                    "Core download failed (HTTP ${connection.responseCode}). Check your connection and try again.",
                )
            }

            val total = connection.contentLengthLong
            connection.inputStream.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        copied += read
                        if (total > 0) onProgress?.invoke(copied.toFloat() / total)
                    }
                }
            }

            ZipInputStream(temp.inputStream().buffered()).use { zip ->
                var entry = zip.nextEntry
                var wrote = false
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".so")) {
                        staged.outputStream().use { output -> zip.copyTo(output) }
                        wrote = true
                        break
                    }
                    entry = zip.nextEntry
                }
                if (!wrote) throw CoreDownloadException("The downloaded core archive was empty.")
            }

            if (!staged.renameTo(target)) {
                staged.copyTo(target, overwrite = true)
                staged.delete()
            }
            target.setReadable(true, false)
            target.setExecutable(true, false)
            onProgress?.invoke(1f)
            return target
        } catch (e: CoreDownloadException) {
            throw e
        } catch (e: Exception) {
            throw CoreDownloadException(
                "Could not install the ${system.shortName} core. Please try again.",
                e,
            )
        } finally {
            temp.delete()
            staged.delete()
        }
    }

    private val abi: String
        get() = Build.SUPPORTED_ABIS.firstOrNull { it in SUPPORTED_ABIS } ?: "arm64-v8a"

    private companion object {
        val SUPPORTED_ABIS = setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
    }
}

internal object BuildConfigVersion {
    const val VERSION = "1.0.0"
}
