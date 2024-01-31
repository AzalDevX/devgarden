package com.azaldev.garden.classes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Auth (
    @PrimaryKey
    val id: Int = 1,

    val email: String,
    val password: String,
    val code: String?,
    val server_synced: Boolean
)