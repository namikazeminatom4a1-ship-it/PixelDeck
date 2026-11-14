package dev.pixeldeck

import android.app.Application
import android.content.Context
import dev.pixeldeck.data.db.PixelDeckDatabase
import dev.pixeldeck.data.repo.FolderStore
import dev.pixeldeck.data.repo.GameRepository
import dev.pixeldeck.data.repo.RomScanner
import dev.pixeldeck.data.settings.SettingsRepository
import dev.pixeldeck.emulation.CoreManager
import dev.pixeldeck.emulation.EmulationSession
import dev.pixeldeck.emulation.SaveManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = PixelDeckDatabase.get(appContext)

    val games = GameRepository(database.gameDao())
    val scanner = RomScanner(appContext, games)
    val folders = FolderStore(appContext)
    val settings = SettingsRepository(appContext)
    val coreManager = CoreManager(appContext)
    val saves = SaveManager(appContext)
    val session = EmulationSession(appContext, coreManager, saves)
}

class PixelDeckApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
