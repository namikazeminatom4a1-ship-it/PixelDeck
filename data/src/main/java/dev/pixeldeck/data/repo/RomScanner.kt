package dev.pixeldeck.data.repo

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.pixeldeck.data.model.SystemCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RomScanner(
    private val context: Context,
    private val repository: GameRepository,
) {

    suspend fun scan(treeUri: Uri): ScanResult = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: return@withContext ScanResult(0, 0, 0, "Could not open that folder.")

        val found = mutableListOf<ScannedGame>()
        val hintPrefix = root.name?.let { "$it/" } ?: ""
        walk(root, treeUri, "", found, hintPrefix)
        repository.importScanned(found)
    }

    private fun walk(
        dir: DocumentFile,
        treeUri: Uri,
        relativePath: String,
        out: MutableList<ScannedGame>,
        hintPrefix: String,
    ) {
        val children = dir.listFiles()
        val byName = children.mapNotNull { child ->
            child.name?.lowercase()?.let { it to child }
        }.toMap()

        val hasCue = children.any { it.name?.endsWith(".cue", ignoreCase = true) == true }

        for (file in children) {
            val name = file.name ?: continue
            if (name.startsWith(".")) continue

            if (file.isDirectory) {
                walk(file, treeUri, "$relativePath$name/", out, hintPrefix)
                continue
            }

            val extension = name.substringAfterLast('.', "").lowercase()
            if (extension.isEmpty()) continue

            val hintPath = hintPrefix + relativePath + name
            val sizeBytes = file.length()
            var system = SystemCatalog.resolve(extension, hintPath, sizeBytes) ?: continue

            if (extension == "iso") {
                IsoProbe.detect(context, file.uri)?.let { detected ->
                    SystemCatalog.byId[detected]?.let { system = it }
                }
            }

            if (hasCue && extension in MULTI_FILE_SIDECARS) continue
            if (extension == "m3u" || (extension == "gdi" && hasCue)) continue

            val discUris = if (extension == "cue") parseCueReferences(file, byName) else emptyList()

            out += ScannedGame(
                title = cleanTitle(name),
                fileName = name,
                uri = file.uri,
                treeUri = treeUri,
                relativePath = relativePath + name,
                systemId = system.id,
                sizeBytes = sizeBytes,
                lastModified = file.lastModified(),
                discUris = discUris,
            )
        }
    }

    private fun parseCueReferences(
        cueFile: DocumentFile,
        siblings: Map<String, DocumentFile>,
    ): List<Uri> {
        return try {
            val text = context.contentResolver.openInputStream(cueFile.uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: return emptyList()

            FILE_PATTERN.findAll(text)
                .mapNotNull { match -> siblings[match.groupValues[1].trim().lowercase()]?.uri }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun cleanTitle(fileName: String): String {
        val base = fileName.substringBeforeLast('.')
        val stripped = base.replace(TAG_PATTERN, "").trim()
        return stripped.ifBlank { base }
    }

    private companion object {
        val MULTI_FILE_SIDECARS = setOf("bin", "img", "raw", "sub", "ccd")
        val FILE_PATTERN = Regex(
            """FILE\s+"?([^"\r\n]+?)"?\s+(?:BINARY|MOTOROLA|WAVE|MP3|AIFF)""",
            RegexOption.IGNORE_CASE,
        )
        val TAG_PATTERN = Regex("""\s*\([^)]*\)""")
    }
}
