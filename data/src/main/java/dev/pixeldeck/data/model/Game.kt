package dev.pixeldeck.data.model

import android.net.Uri

data class Game(
    val id: Long,
    val title: String,
    val fileName: String,
    val uri: Uri,
    val treeUri: Uri,
    val relativePath: String,
    val system: GameSystem,
    val sizeBytes: Long,
    val lastModified: Long,
    val addedAt: Long,
    val lastPlayedAt: Long?,
    val playTimeMillis: Long,
    val favorite: Boolean,
    val discUris: List<Uri>,
) {
    val artworkKey: String get() = fileName.substringBeforeLast('.')
    val artworkUrl: String? get() = Artwork.url(system, artworkKey)
}
