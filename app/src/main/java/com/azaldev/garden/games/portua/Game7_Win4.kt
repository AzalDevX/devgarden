package com.azaldev.garden.games.portua

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game7_Win4 : AppCompatActivity() {
    private lateinit var juegoView: JuegoView
    private lateinit var btnEmpezarJuego: Button
    private lateinit var scoreTextView: TextView
    private lateinit var drawingImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game7_win4)

        btnEmpezarJuego = findViewById(R.id.btnEmpezarJuego)
        juegoView = findViewById(R.id.juegoView)

        btnEmpezarJuego.setOnClickListener {
            btnEmpezarJuego.visibility = View.GONE
            juegoView.reiniciarJuego()
        }
        val game_id = 7;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game61", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game7_Win4, next_game)
                    else
                        Utilities.startActivity(this@Game7_Win4, LandingActivity::class.java)

                    finish()
                }
            }
        }
    }

}
