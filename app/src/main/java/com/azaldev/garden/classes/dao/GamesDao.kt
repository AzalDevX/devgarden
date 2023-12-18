package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM Game")
    fun getGames(): List<Game>

    @Query("SELECT * FROM Game WHERE id = :id")
    fun getGame(id: Int): Game

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(newGame: Game)

    @Query("SELECT COUNT(*) FROM Game WHERE id = :id")
    fun exists(id: Int): Int

    @Query("DELETE FROM Auth WHERE id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM Auth")
    fun deleteAll()
}
