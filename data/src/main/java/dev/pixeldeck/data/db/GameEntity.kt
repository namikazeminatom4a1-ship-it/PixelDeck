package dev.pixeldeck.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [
        Index(value = ["systemId"]),
        Index(value = ["uri"], unique = true),
    ],
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileName: String,
    val uri: String,
    val treeUri: String,
    val relativePath: String,
    val systemId: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val addedAt: Long,
    val lastPlayedAt: Long? = null,
    val playTimeMillis: Long = 0L,
    val favorite: Boolean = false,
    val discUris: String = "",
    val systemOverride: Boolean = false,
)
