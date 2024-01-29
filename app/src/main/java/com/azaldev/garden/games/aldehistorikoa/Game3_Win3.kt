package com.azaldev.garden.games.aldehistorikoa

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game3_Win3 : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var barrenImageView: ImageView
    private lateinit var bakailaoaImageView: ImageView
    private lateinit var kristoImageView: ImageView
    private lateinit var burdinImageView: ImageView
    private lateinit var elizaImageView: ImageView
    private lateinit var sardinaImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_win3)

        // Inicializar las vistas
        gameView = findViewById(R.id.gameView)
        barrenImageView = findViewById(R.id.barren)
        bakailaoaImageView = findViewById(R.id.bakailaoa)
        kristoImageView = findViewById(R.id.kristo)
        burdinImageView = findViewById(R.id.burdin)
        elizaImageView = findViewById(R.id.eliza)
        sardinaImageView = findViewById(R.id.sardina)

        // Configurar el onTouchListener para las imágenes
        setOnTouchListener(barrenImageView)
        setOnTouchListener(bakailaoaImageView)
        setOnTouchListener(kristoImageView)
        setOnTouchListener(burdinImageView)
        setOnTouchListener(elizaImageView)
        setOnTouchListener(sardinaImageView)

        // Inicializar el contexto de la actividad en GameView
        gameView.initActivityContext(this)
        
        val game_id = 3;
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao();

        findViewById<Button>(R.id.next_game).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                gameDao.adv_progress(game_id, 1);

                val next_game = current_game.getActivityProgress();
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game33", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game3_Win3, next_game)
                    else
                        Utilities.startActivity(this@Game3_Win3, LandingActivity::class.java)

                    finish()
                }
            }
    }

    private fun setOnTouchListener(imageView: ImageView) {
        imageView.setOnTouchListener { _, event ->
            // Obtener las coordenadas de la imagen
            val x = event.x + imageView.x
            val y = event.y + imageView.y

            // Pasar las coordenadas al GameView para dibujar la línea
            gameView.handleTouchEvent(event.action, x, y)

            // Devolver true para indicar que el evento fue manejado
            true
        }
    }
}

