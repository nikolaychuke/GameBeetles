package com.example.beetles.data

import androidx.room.Embedded
import androidx.room.Relation

data class ScoreWithPlayer(
    @Embedded val score: ScoreEntity,

    @Relation(
        parentColumn = "playerId",
        entityColumn = "id"
    )
    val player: PlayerEntity
)