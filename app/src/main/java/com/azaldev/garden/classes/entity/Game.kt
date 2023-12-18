package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Game (
    @PrimaryKey
    val id: Int = 1,
    val x: Double,
    val y: Double,
    val name: String,
    val image: Int,
    val progress: Int = 0,
    val max_progress: Int = 6,
    val isLocked: Boolean,
    val isFinished: Boolean,
)