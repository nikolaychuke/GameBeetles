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

    @Query("SELECT * FROM scores WHERE playerId = :playerId LIMIT 1")
    suspend fun getScoreByPlayerId(playerId: Long): ScoreEntity?

    @Update
    suspend fun updateScore(score: ScoreEntity)

    @Transaction
    @Query("""
        SELECT * FROM scores s
        WHERE s.score = (
            SELECT MAX(s2.score) FROM scores s2 WHERE s2.playerId = s.playerId
        )
        GROUP BY s.playerId
        ORDER BY s.score DESC, s.timestamp DESC
    """)
    suspend fun getBestScorePerPlayer(): List<ScoreWithPlayer>

    @Transaction
    @Query("SELECT * FROM scores ORDER BY score DESC, timestamp DESC")
    suspend fun getAllScoresWithPlayers(): List<ScoreWithPlayer>

    @Transaction
    @Query("SELECT * FROM scores WHERE playerId = :playerId ORDER BY score DESC")
    suspend fun getScoresForPlayer(playerId: Long): List<ScoreWithPlayer>
}