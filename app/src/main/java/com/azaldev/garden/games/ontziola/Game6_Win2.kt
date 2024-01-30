package com.azaldev.garden.games.ontziola

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game6_Win2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game6_win2)

        val game_id = 6;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();
        val player_button = findViewById<ImageButton>(R.id.player_button)
        val next_game = findViewById<Button>(R.id.next_game)

        next_game.visibility = INVISIBLE

        next_game.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game62", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game6_Win2, next_game)
                    else
                        Utilities.startActivity(this@Game6_Win2, LandingActivity::class.java)

                    finish()
                }
            }
        }

        player_button.setOnClickListener {
                player_button.visibility = INVISIBLE
            Utilities.playSound(this,R.raw.game6_win2){
                next_game.visibility = VISIBLE
            }
        }
    }
}