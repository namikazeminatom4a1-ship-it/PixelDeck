package dev.pixeldeck.data.model

object SystemCatalog {

    val all: List<GameSystem> = listOf(
        GameSystem(
            id = "nes",
            displayName = "Nintendo Entertainment System",
            shortName = "NES",
            manufacturer = "Nintendo",
            year = 1983,
            core = CoreSpec("fceumm"),
            extensions = setOf("nes", "fds", "unf", "unif"),
            thumbnailDir = "Nintendo_-_Nintendo_Entertainment_System",
            accentSeed = 0xFFE45B5B,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "snes",
            displayName = "Super Nintendo Entertainment System",
            shortName = "SNES",
            manufacturer = "Nintendo",
            year = 1990,
            core = CoreSpec("snes9x"),
            extensions = setOf("sfc", "smc", "swc", "fig", "bs"),
            thumbnailDir = "Nintendo_-_Super_Nintendo_Entertainment_System",
            accentSeed = 0xFF8E7BFF,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "gb",
            displayName = "Game Boy",
            shortName = "GB",
            manufacturer = "Nintendo",
            year = 1989,
            core = CoreSpec("gambatte"),
            extensions = setOf("gb"),
            thumbnailDir = "Nintendo_-_Game_Boy",
            accentSeed = 0xFF8FA36B,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "gbc",
            displayName = "Game Boy Color",
            shortName = "GBC",
            manufacturer = "Nintendo",
            year = 1998,
            core = CoreSpec("gambatte"),
            extensions = setOf("gbc"),
            thumbnailDir = "Nintendo_-_Game_Boy_Color",
            accentSeed = 0xFFF2B84B,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "gba",
            displayName = "Game Boy Advance",
            shortName = "GBA",
            manufacturer = "Nintendo",
            year = 2001,
            core = CoreSpec("mgba"),
            extensions = setOf("gba"),
            thumbnailDir = "Nintendo_-_Game_Boy_Advance",
            accentSeed = 0xFF6C63FF,
            maxPlayers = 4,
        ),
        GameSystem(
            id = "genesis",
            displayName = "Sega Genesis / Mega Drive",
            shortName = "GEN",
            manufacturer = "Sega",
            year = 1988,
            core = CoreSpec("genesis_plus_gx"),
            extensions = setOf("md", "gen", "smd"),
            thumbnailDir = "Sega_-_Mega_Drive_-_Genesis",
            accentSeed = 0xFF3D8BFF,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "sms",
            displayName = "Sega Master System",
            shortName = "SMS",
            manufacturer = "Sega",
            year = 1985,
            core = CoreSpec("genesis_plus_gx"),
            extensions = setOf("sms"),
            thumbnailDir = "Sega_-_Master_System_-_Mark_III",
            accentSeed = 0xFF4FC3F7,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "gg",
            displayName = "Game Gear",
            shortName = "GG",
            manufacturer = "Sega",
            year = 1990,
            core = CoreSpec("genesis_plus_gx"),
            extensions = setOf("gg"),
            thumbnailDir = "Sega_-_Game_Gear",
            accentSeed = 0xFF7E57C2,
            maxPlayers = 2,
        ),
        GameSystem(
            id = "psx",
            displayName = "PlayStation",
            shortName = "PS1",
            manufacturer = "Sony",
            year = 1994,
            core = CoreSpec("pcsx_rearmed", requiresBios = true),
            extensions = setOf("chd", "pbp", "cue", "bin", "img", "iso"),
            thumbnailDir = "Sony_-_PlayStation",
            accentSeed = 0xFFB0BEC5,
            folderHints = setOf("ps1", "psx", "playstation", "psone"),
            maxPlayers = 2,
            notes = "Requires a PlayStation BIOS file. .chd or .pbp recommended.",
        ),
        GameSystem(
            id = "n64",
            displayName = "Nintendo 64",
            shortName = "N64",
            manufacturer = "Nintendo",
            year = 1996,
            core = CoreSpec("mupen64plus_next_gles3", experimental = true),
            extensions = setOf("n64", "z64", "v64"),
            thumbnailDir = "Nintendo_-_Nintendo_64",
            accentSeed = 0xFF4DB6AC,
            maxPlayers = 4,
            experimental = true,
            notes = "Performance varies by device. A gamepad is strongly recommended.",
        ),
        GameSystem(
            id = "nds",
            displayName = "Nintendo DS",
            shortName = "NDS",
            manufacturer = "Nintendo",
            year = 2004,
            core = CoreSpec("melonds", experimental = true),
            extensions = setOf("nds"),
            thumbnailDir = "Nintendo_-_Nintendo_DS",
            accentSeed = 0xFFFF8A65,
            maxPlayers = 2,
            preferredOrientation = PreferredOrientation.PORTRAIT,
            experimental = true,
            notes = "Touch screen input may be unavailable in this build.",
        ),
        GameSystem(
            id = "psp",
            displayName = "PlayStation Portable",
            shortName = "PSP",
            manufacturer = "Sony",
            year = 2004,
            core = CoreSpec("ppsspp", experimental = true),
            extensions = setOf("iso", "cso", "pbp"),
            thumbnailDir = "Sony_-_PlayStation_Portable",
            accentSeed = 0xFF7986CB,
            folderHints = setOf("psp", "playstation portable"),
            maxPlayers = 2,
            experimental = true,
        ),
        GameSystem(
            id = "dreamcast",
            displayName = "Dreamcast",
            shortName = "DC",
            manufacturer = "Sega",
            year = 1998,
            core = CoreSpec("flycast", experimental = true),
            extensions = setOf("gdi", "cdi", "chd", "m3u"),
            thumbnailDir = "Sega_-_Dreamcast",
            accentSeed = 0xFFFF7043,
            folderHints = setOf("dreamcast", "dc"),
            maxPlayers = 4,
            experimental = true,
        ),
        GameSystem(
            id = "ps2",
            displayName = "PlayStation 2",
            shortName = "PS2",
            manufacturer = "Sony",
            year = 2000,
            core = CoreSpec("pcsx2", experimental = true),
            extensions = setOf("iso", "chd", "bin"),
            thumbnailDir = "Sony_-_PlayStation_2",
            accentSeed = 0xFF90A4AE,
            folderHints = setOf("ps2", "playstation 2"),
            maxPlayers = 2,
            experimental = true,
            notes = "Experimental. Only a handful of devices sustain playable speed.",
        ),
        GameSystem(
            id = "arcade",
            displayName = "Arcade",
            shortName = "ARC",
            manufacturer = "Various",
            year = 1979,
            core = CoreSpec("fbneo", experimental = true),
            extensions = setOf("zip"),
            thumbnailDir = "",
            accentSeed = 0xFFFFD166,
            folderHints = setOf("arcade", "mame", "fbneo", "cps"),
            maxPlayers = 4,
            experimental = true,
            notes = "Uses FBNeo. ROM sets must match the core version.",
        ),
    )

    val byId: Map<String, GameSystem> = all.associateBy { it.id }

    val byExtension: Map<String, List<GameSystem>> = buildMap {
        all.forEach { system ->
            system.extensions.forEach { ext ->
                put(ext, (get(ext) ?: emptyList()) + system)
            }
        }
    }

    val scanExtensions: Set<String> = byExtension.keys

    fun resolve(extension: String, path: String, sizeBytes: Long = 0L): GameSystem? {
        val candidates = byExtension[extension.lowercase()] ?: return null
        if (candidates.size == 1) return candidates.first()
        val lowerPath = path.lowercase()
        candidates.firstOrNull { system ->
            system.folderHints.any { hint -> lowerPath.contains(hint) }
        }?.let { return it }

        if (extension.equals("iso", ignoreCase = true) && sizeBytes > 0L) {
            if (sizeBytes > 2_000_000_000L) byId["ps2"]?.let { return it }
            if (sizeBytes > 800_000_000L) byId["psp"]?.let { return it }
        }

        return candidates.firstOrNull { !it.experimental } ?: candidates.first()
    }

    fun forGameList(systemIds: Set<String>): List<GameSystem> =
        all.filter { it.id in systemIds }.sortedBy { it.displayName }
}
