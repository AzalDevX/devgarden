package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Game (
    @PrimaryKey
    val id: Int = 1,
    val coords: Map<String, Double>,
    val name: String,
    val image: Int,
    val isLocked: Boolean,
    val isFinished: Boolean,
)