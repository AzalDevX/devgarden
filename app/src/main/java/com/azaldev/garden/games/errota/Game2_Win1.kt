package com.azaldev.garden.games.errota

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.-
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game2_Win1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_win1)

        val player_button = findViewById<ImageButton>(R.id.player_button)
        val next_game = findViewById<Button>(R.id.next_game)
        next_game.visibility = INVISIBLE

        player_button.setOnClickListener {
                Utilities.playSound(this, R.raw.audio_game2_win1) {
                    player_button.visibility = VISIBLE
                    next_game.visibility = VISIBLE
                }
            player_button.visibility = INVISIBLE
        }

        val game_id = 2;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game21", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game2_Win1, next_game)
                    else
                        Utilities.startActivity(this@Game2_Win1, LandingActivity::class.java)

                    finish()
                }
            }
        }
    }
}