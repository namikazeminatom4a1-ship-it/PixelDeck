package dev.pixeldeck.data.model

import android.net.Uri

object Artwork {

    fun url(system: GameSystem, artworkKey: String): String? {
        if (system.thumbnailDir.isEmpty() || artworkKey.isBlank()) return null
        val name = Uri.encode("$artworkKey.png")
        return "https://raw.githubusercontent.com/libretro-thumbnails/" +
            "${system.thumbnailDir}/master/Named_Boxarts/$name"
    }
}
