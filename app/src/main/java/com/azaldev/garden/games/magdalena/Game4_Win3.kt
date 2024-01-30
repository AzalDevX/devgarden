package com.azaldev.garden.games.magdalena

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game4_Win3 : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game4_win3)

        videoView = findViewById(R.id.videoView)
        mediaController = MediaController(this)

        // Asigna el MediaController al VideoView
        videoView.setMediaController(mediaController)

        // Establece la ruta del video desde los recursos raw
        val path = "android.resource://" + packageName + "/" + R.raw.alunelul
        videoView.setVideoPath(path)

        // Inicia la reproducción del video
        videoView.start()

        // Mostrar el MediaController y vincularlo al VideoView
        mediaController.show(0)
        mediaController.setAnchorView(videoView)


        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        val game_id = 4;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game43", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game4_Win3, next_game)
                    else
                        Utilities.startActivity(this@Game4_Win3, LandingActivity::class.java)

                    finish()
                }
            }
        }
    }
}