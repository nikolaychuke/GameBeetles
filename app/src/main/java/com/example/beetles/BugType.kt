package com.example.beetles

import android.graphics.Color

enum class BugType(
    val points: Int,
    val speedFactor: Float,
    val sizeFactor: Float,
    val color: Int,
    val spawnWeight: Int
) {
    COMMON(
        points = 1,
        speedFactor = 1.0f,
        sizeFactor = 1.0f,
        color = Color.rgb(180, 60, 60),
        spawnWeight = 45
    ),
    FAST(
        points = 3,
        speedFactor = 2.0f,
        sizeFactor = 0.8f,
        color = Color.rgb(60, 140, 60),
        spawnWeight = 45
    ),
    RARE(
        points = 5,
        speedFactor = 1.5f,
        sizeFactor = 1.5f,
        color = Color.rgb(150, 80, 180),
        spawnWeight = 10
    )
}