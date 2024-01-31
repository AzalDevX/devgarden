package com.azaldev.garden.games.portua

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Game7_Win3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game7_win3)

        val game_id = 7;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();


        findViewById<Button>(R.id.saveButton).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game61", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game7_Win3, next_game)
                    else
                        Utilities.startActivity(this@Game7_Win3, LandingActivity::class.java)

                    finish()
                }
            }
        }
        val drawingView = findViewById<DrawingView>(R.id.drawingView)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        clearButton.setOnClickListener {
            drawingView.clearCanvas()
        }

    }

}
