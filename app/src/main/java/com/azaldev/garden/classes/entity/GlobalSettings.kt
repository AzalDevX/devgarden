package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GlobalSettings(
    @PrimaryKey
    val id: Int = 1,

    val lang: String?,
    val theme: String?,
)
