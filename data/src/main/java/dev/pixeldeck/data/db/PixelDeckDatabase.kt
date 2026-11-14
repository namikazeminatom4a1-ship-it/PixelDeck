package dev.pixeldeck.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PixelDeckDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var instance: PixelDeckDatabase? = null

        fun get(context: Context): PixelDeckDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PixelDeckDatabase::class.java,
                    "pixeldeck.db",
                ).build().also { instance = it }
            }
    }
}
