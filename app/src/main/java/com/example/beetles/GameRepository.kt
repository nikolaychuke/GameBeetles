package com.example.beetles.data

import android.content.Context

class GameRepository(context: Context) {

    private val dao = GameDatabase.get(context).gameDao()

    suspend fun registerPlayer(player: PlayerEntity): Long = dao.insertPlayer(player)

    suspend fun getAllPlayers(): List<PlayerEntity> = dao.getAllPlayers()

    suspend fun saveScore(score: ScoreEntity): Long {
        val existing = dao.getScoreByPlayerId(score.playerId)

        return if (existing == null) {
            dao.insertScore(score)
        } else if (score.score > existing.score) {
            val updated = existing.copy(
                score = score.score,
                hits = score.hits,
                misses = score.misses,
                accuracy = score.accuracy,
                difficulty = score.difficulty,
                timestamp = score.timestamp
            )
            dao.updateScore(updated)
            updated.id
        } else {
            existing.id
        }
    }

    suspend fun getAllScores(): List<ScoreWithPlayer> = dao.getBestScorePerPlayer()
}