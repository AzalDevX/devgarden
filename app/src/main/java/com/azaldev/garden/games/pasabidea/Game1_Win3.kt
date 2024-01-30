package com.azaldev.garden.games.pasabidea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game1_Win3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_win3)

        val button = findViewById<Button>(R.id.next_game)
        val customView = findViewById<CustomView>(R.id.customView)

        val game_id = 1;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();

        // Asocia el evento clic al botón para hacerlo visible
        button.setOnClickListener {
            button.visibility = View.VISIBLE
        }

        // Asocia el evento de toque en CustomView para hacer visible el botón
        customView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    // Verifica si el usuario ha conectado los puntos en el orden correcto
                    if (customView.isCorrectOrder()) {
                        button.visibility = View.VISIBLE
                    }
                }
            }
            false
        }

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game33", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game1_Win3, next_game)
                    else
                        Utilities.startActivity(this@Game1_Win3, LandingActivity::class.java)

                    finish()
                }
            }
        }

    }
}