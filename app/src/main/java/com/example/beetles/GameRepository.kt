package com.example.beetles.data

import android.content.Context

class GameRepository(context: Context) {

    private val dao = GameDatabase.get(context).gameDao()

    suspend fun registerPlayer(player: PlayerEntity): Long = dao.insertPlayer(player)

    suspend fun getAllPlayers(): List<PlayerEntity> = dao.getAllPlayers()

    suspend fun saveScore(score: ScoreEntity): Long = dao.insertScore(score)

    suspend fun getAllScores(): List<ScoreWithPlayer> = dao.getAllScoresWithPlayers()
}