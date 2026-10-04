package com.example.beetles

data class GameState(
    val score: Int,
    val hits: Int,
    val misses: Int,
    val timeLeft: Int,
    val bugs: List<Bug>,
    val isRunning: Boolean
)