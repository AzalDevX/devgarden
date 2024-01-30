package com.azaldev.garden.games.errota

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game2_Win3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.azaldev.garden.R.layout.activity_game2_win3)

        val garia = findViewById<ImageView>(com.azaldev.garden.R.id.garia)
        val irina = findViewById<ImageView>(com.azaldev.garden.R.id.irina)
        val constraintLayout = findViewById<ConstraintLayout>(com.azaldev.garden.R.id.clayout)

        garia.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Inicia el arrastre
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDrag(data, shadowBuilder, view, 0)
                true
            } else {
                false
            }
        }

        irina.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Inicia el arrastre
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDrag(data, shadowBuilder, view, 0)
                true
            } else {
                false
            }
        }

        constraintLayout.setOnDragListener { v, event ->
            val action = event.action
            when (action) {
                DragEvent.ACTION_DROP -> {
                    // Maneja la lógica después de soltar la imagen
                    val draggedView = event.localState as View
                    // Obtén las coordenadas de la gota
                    val x = event.x
                    val y = event.y

                    // Actualiza las restricciones de la vista arrastrada
                    val layoutParams = ConstraintLayout.LayoutParams(
                        draggedView.width,
                        draggedView.height
                    )
                    layoutParams.leftMargin = (x - draggedView.width / 2).toInt()
                    layoutParams.topMargin = (y - draggedView.height / 2).toInt()
                    draggedView.layoutParams = layoutParams

                    // Actualiza las restricciones según sea necesario
                    // (puedes personalizar esta parte según tus requisitos)

                    true
                }
                else -> true
            }
        }
    }
}
