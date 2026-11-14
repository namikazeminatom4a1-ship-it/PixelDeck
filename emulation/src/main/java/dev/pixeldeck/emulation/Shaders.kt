package dev.pixeldeck.emulation

import com.swordfish.libretrodroid.ShaderConfig
import dev.pixeldeck.data.settings.ShaderChoice

object Shaders {
    fun config(choice: ShaderChoice): ShaderConfig = when (choice) {
        ShaderChoice.DEFAULT -> ShaderConfig.Default
        ShaderChoice.CRT -> ShaderConfig.CRT
        ShaderChoice.LCD -> ShaderConfig.LCD
        ShaderChoice.SHARP -> ShaderConfig.Sharp
    }
}
