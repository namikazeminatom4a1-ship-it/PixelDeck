package dev.pixeldeck.data.model

enum class PreferredOrientation { LANDSCAPE, PORTRAIT }

data class CoreSpec(
    val name: String,
    val experimental: Boolean = false,
    val requiresBios: Boolean = false,
) {
    val soName: String get() = "${name}_libretro_android.so"
    val zipName: String get() = "${name}_libretro_android.so.zip"
}

data class GameSystem(
    val id: String,
    val displayName: String,
    val shortName: String,
    val manufacturer: String,
    val year: Int,
    val core: CoreSpec,
    val extensions: Set<String>,
    val thumbnailDir: String,
    val accentSeed: Long,
    val folderHints: Set<String> = emptySet(),
    val maxPlayers: Int = 1,
    val preferredOrientation: PreferredOrientation = PreferredOrientation.LANDSCAPE,
    val experimental: Boolean = false,
    val notes: String? = null,
) {
    val requiresBios: Boolean get() = core.requiresBios
}
