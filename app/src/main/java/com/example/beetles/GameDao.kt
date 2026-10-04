package com.example.beetles.data

import androidx.room.*

@Dao
interface GameDao {

    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("SELECT * FROM players ORDER BY id DESC")
    suspend fun getAllPlayers(): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE fio = :fio LIMIT 1")
    suspend fun findPlayerByFio(fio: String): PlayerEntity?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Insert
    suspend fun insertScore(score: ScoreEntity): Long

    @Transaction
    @Query("SELECT * FROM scores ORDER BY score DESC, timestamp DESC")
    suspend fun getAllScoresWithPlayers(): List<ScoreWithPlayer>

    @Transaction
    @Query("SELECT * FROM scores WHERE playerId = :playerId ORDER BY score DESC")
    suspend fun getScoresForPlayer(playerId: Long): List<ScoreWithPlayer>
}