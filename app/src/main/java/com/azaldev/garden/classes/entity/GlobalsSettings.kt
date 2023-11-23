package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_settings")
data class GlobalSettings(
    @PrimaryKey val id: Long = 1,
    val lang: String,
    val theme: String,
    val lastUser: String,
    val isLogged: Boolean,
    val email: String,
    val passwd: String
)
