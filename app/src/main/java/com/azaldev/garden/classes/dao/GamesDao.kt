package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.Auth

@Dao
interface GamesDao {

    @Query("SELECT * FROM Games ")
    fun getGames(): Games?

}
