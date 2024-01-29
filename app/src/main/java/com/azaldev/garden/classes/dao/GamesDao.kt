package com.azaldev.garden.classes.dao

import androidx.room.*
import com.azaldev.garden.classes.entity.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM Game")
    fun getGames(): List<Game>

    @Query("SELECT * FROM Game WHERE id = :id")
    fun getGame(id: Int): Game
    @Query("UPDATE Game SET progress = progress + :by WHERE id = :id")
    fun adv_progress(id: Int, by: Int) // Add progress to the game

    @Query("UPDATE Game SET progress = 1, isFinished = 0, isLocked = 0 WHERE id = :id")
    fun reset_progress(id: Int)

    @Query("UPDATE Game SET isFinished = 1 WHERE id = :id")
    fun finish_game(id: Int)

    @Query("UPDATE Game SET isLocked = 0 WHERE id = :id + 1")
    fun unlock_nextgame(id: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(newGame: Game)

    @Query("SELECT COUNT(*) FROM Game WHERE id = :id")
    fun exists(id: Int): Int

    @Query("DELETE FROM Game WHERE id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM Game")
    fun deleteAll()
}
