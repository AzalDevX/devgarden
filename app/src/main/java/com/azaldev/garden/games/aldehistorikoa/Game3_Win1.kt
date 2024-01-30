package com.azaldev.garden.games.aldehistorikoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.classes.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game3_Win1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_win1)
        
        val game_id = 3;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();
        
        val player_button = findViewById<ImageButton>(R.id.player_button)
        val next_game_btn = findViewById<Button>(R.id.next_game)

        next_game_btn.visibility = INVISIBLE

        player_button.setOnClickListener {
            Utilities.playSound(this,R.raw.game3_win1)
            player_button.visibility = INVISIBLE
            next_game_btn.visibility = VISIBLE
        }

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game31", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game3_Win1, next_game)
                    else
                        Utilities.startActivity(this@Game3_Win1, LandingActivity::class.java)

                    finish()
                }
            }
        }
    }
}