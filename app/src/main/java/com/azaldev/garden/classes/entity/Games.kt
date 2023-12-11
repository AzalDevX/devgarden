package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Games (
    @PrimaryKey
    val id: Int = 1,
    val x: Float,
    val y: Float,
    val name: String,
    val image: Int,
    val isLocked: Boolean,
    val isFinished: Boolean,
)