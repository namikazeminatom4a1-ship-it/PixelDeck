package dev.pixeldeck.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun observe(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun get(id: Long): GameEntity?

    @Query("SELECT * FROM games")
    suspend fun getAllOnce(): List<GameEntity>

    @Query("SELECT * FROM games WHERE uri = :uri LIMIT 1")
    suspend fun findByUri(uri: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>): List<Long>

    @Update
    suspend fun update(game: GameEntity)

    @Query("UPDATE games SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE games SET systemId = :systemId, systemOverride = 1 WHERE id = :id")
    suspend fun setSystem(id: Long, systemId: String)

    @Query("UPDATE games SET lastPlayedAt = :time WHERE id = :id")
    suspend fun markPlayed(id: Long, time: Long)

    @Query("UPDATE games SET playTimeMillis = playTimeMillis + :delta WHERE id = :id")
    suspend fun addPlayTime(id: Long, delta: Long)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM games")
    suspend fun clear()
}
