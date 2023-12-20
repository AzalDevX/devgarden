package com.azaldev.garden.globals

import android.content.Context
import com.azaldev.garden.R
import com.azaldev.garden.classes.entity.Game
import com.azaldev.garden.classes.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.*
import com.azaldev.garden.games.pasabidea.PasabideaGame
import kotlinx.coroutines.launch

class GameManager(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    private val database = AppDatabase.getInstance(context)
    private val gameDao = database.GameDao();

    private val gameList: List<Game> = listOf(
        Game(
            id = 1,
            x = 43.40257,
            y = -2.94652,
            name = "Pasabidea",
            activityClassName = "com.azaldev.garden.games.pasabidea.PasabideaGame",
            image = R.drawable.pasabidea,
            max_progress = 6,
            isLocked = false,
            isFinished = false
        ),
        Game(
            id = 2,
            x = 43.4029,
            y = -2.94519,
            name = "Errota",
            image = R.drawable.errota,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 3,
            x = 43.40469,
            y = -2.94762,
            name = "Alde Historikoa",
            image = R.drawable.zaharra,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 4,
            x = 43.40519,
            y = -2.94778,
            name = "Magdalena Eliza",
            image = R.drawable.madalena,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 5,
            x =  43.40545,
            y = -2.94772,
            name = "Santiago Arkua",
            image = R.drawable.arkua,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 6,
            x = 43.40436,
            y = -2.94983,
            name = "Ontziola",
            image = R.drawable.plaza,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 7,
            x = 43.40739,
            y = -2.94522,
            name = "Portua/Hondartza",
            image = R.drawable.hondartza,
            max_progress = 6,
            isLocked = true,
            isFinished = true
        ),
    )

    fun initializeGames() {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val existingGames = gameDao.getGames()
            if (existingGames.isEmpty()) {
                for (game in gameList) {
                    gameDao.insert(game)
                }
            }
        }
    }

    // Method to check if a game is initialized
    fun isGameInitialized(gameId: Int, callback: (Boolean) -> Unit) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val isInitialized = gameDao.exists(gameId) > 0
            withContext(Dispatchers.Main) {
                callback(isInitialized)
            }
        }
    }

    // Method to delete all games from the Room database
    fun deleteAllGames() {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            gameDao.deleteAll()
        }
    }
}
