package com.azaldev.garden.games.pasabidea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.azaldev.garden.R

class Game1_Win3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_win3)

        val button = findViewById<Button>(R.id.next_game)
        val customView = findViewById<CustomView>(R.id.customView)

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

    }
}