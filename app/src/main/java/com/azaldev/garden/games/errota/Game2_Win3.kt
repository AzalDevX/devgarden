package com.azaldev.garden.games.errota

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game2_Win3 : AppCompatActivity() {
    private var uraTouched = false
    private var harriaTouched = false
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.azaldev.garden.R.layout.activity_game2_win3)

        val garia = findViewById<ImageView>(com.azaldev.garden.R.id.garia)
        val irina = findViewById<ImageView>(com.azaldev.garden.R.id.irina)
        val uraButton = findViewById<Button>(R.id.ura_button)
        val harriaButton = findViewById<Button>(R.id.harria_button)
        val constraintLayout = findViewById<ConstraintLayout>(com.azaldev.garden.R.id.clayout)

        // Configura las imágenes para que estén inicialmente invisibles
        garia.visibility = View.INVISIBLE
        irina.visibility = View.INVISIBLE

        // Configura el OnTouchListener para las imágenes
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

        // Agrega OnClickListener al botón "ura_button"
        uraButton.setOnClickListener {
            // Marca que se ha tocado el botón ura
            uraTouched = true
            counter++
            checkAndShowImages()
        }

        // Agrega OnClickListener al botón "harria_button"
        harriaButton.setOnClickListener {
            // Verifica si ya se ha tocado el botón ura antes
            if (uraTouched) {
                // Marca que se ha tocado el botón harria
                harriaTouched = true
                counter++
                checkAndShowImages()
            } else {
                Utilities.showToast(this@Game2_Win3, getString(R.string.game2_water))
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

                    // Obtén las coordenadas del botón correspondiente
                    val gariaButton = findViewById<View>(R.id.button_garia)
                    val irinaButton = findViewById<View>(R.id.button_irina)
                    val gariaButtonCoords = IntArray(2)
                    val irinaButtonCoords = IntArray(2)
                    gariaButton.getLocationOnScreen(gariaButtonCoords)
                    irinaButton.getLocationOnScreen(irinaButtonCoords)

                    // Verifica si las coordenadas de la gota están dentro de los límites del botón correspondiente
                    if (x >= gariaButtonCoords[0] && x <= gariaButtonCoords[0] + gariaButton.width
                        && y >= gariaButtonCoords[1] && y <= gariaButtonCoords[1] + gariaButton.height
                    ) {
                        // Si la gota está sobre el botón de Garia, establece las coordenadas finales
                        draggedView.x = (gariaButtonCoords[0] + gariaButton.width / 2 - draggedView.width / 2).toFloat()
                        draggedView.y = (gariaButtonCoords[1] + gariaButton.height / 2 - draggedView.height / 2).toFloat()

                        // Haz que la imagen sea más pequeña
                        draggedView.layoutParams.width = 250 // nuevo ancho deseado
                        draggedView.layoutParams.height = 250 // nuevo alto deseado
                        draggedView.requestLayout()
                        counter++
                    } else if (x >= irinaButtonCoords[0] && x <= irinaButtonCoords[0] + irinaButton.width
                        && y >= irinaButtonCoords[1] && y <= irinaButtonCoords[1] + irinaButton.height
                    ) {
                        // Si la gota está sobre el botón de Irina, establece las coordenadas finales
                        draggedView.x = (irinaButtonCoords[0] + irinaButton.width / 2 - draggedView.width / 2).toFloat()
                        draggedView.y = (irinaButtonCoords[1] + irinaButton.height / 2 - draggedView.height / 2).toFloat()

                        // Haz que la imagen sea más pequeña
                        draggedView.layoutParams.width = 250 // nuevo ancho deseado
                        draggedView.layoutParams.height = 250// nuevo alto deseado
                        draggedView.requestLayout()
                        counter++
                        if(counter == 4){
                            val game_id = 2;
                            val database = AppDatabase.getInstance(this)
                            val gameDao = database.GameDao();

                            lifecycleScope.launch(Dispatchers.IO) {
                                val current_game = gameDao.getGame(game_id);

                                val next_game = current_game.getActivityProgress(1);
                                gameDao.adv_progress(game_id, 1);
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Log.i("devl|game22", "Moving to the next game")

                                    if (next_game != null)
                                        Utilities.startActivity(this@Game2_Win3, next_game)
                                    else
                                        Utilities.startActivity(this@Game2_Win3, LandingActivity::class.java)

                                    finish()
                                }
                            }
                            }
                        }

                    true
                }
                else -> true
            }
        }
    }

    private fun checkAndShowImages() {
        // Verifica si ambos botones han sido tocados en el orden correcto
        if (uraTouched && harriaTouched) {
            // Hacer que ambas imágenes sean visibles
            findViewById<ImageView>(com.azaldev.garden.R.id.garia).visibility = View.VISIBLE
            findViewById<ImageView>(com.azaldev.garden.R.id.irina).visibility = View.VISIBLE

            // Verifica si las imágenes también se han colocado en las posiciones correctas
            val gariaButton = findViewById<View>(R.id.button_garia)
            val irinaButton = findViewById<View>(R.id.button_irina)
            val gariaImageView = findViewById<ImageView>(com.azaldev.garden.R.id.garia)
            val irinaImageView = findViewById<ImageView>(com.azaldev.garden.R.id.irina)

            val gariaCoords = IntArray(2)
            val irinaCoords = IntArray(2)

            gariaButton.getLocationOnScreen(gariaCoords)
            irinaButton.getLocationOnScreen(irinaCoords)

            val gariaImageX = gariaImageView.x + gariaImageView.width / 2
            val gariaImageY = gariaImageView.y + gariaImageView.height / 2
            val irinaImageX = irinaImageView.x + irinaImageView.width / 2
            val irinaImageY = irinaImageView.y + irinaImageView.height / 2

            if (gariaImageX >= gariaCoords[0] && gariaImageX <= gariaCoords[0] + gariaButton.width
                && gariaImageY >= gariaCoords[1] && gariaImageY <= gariaCoords[1] + gariaButton.height
                && irinaImageX >= irinaCoords[0] && irinaImageX <= irinaCoords[0] + irinaButton.width
                && irinaImageY >= irinaCoords[1] && irinaImageY <= irinaCoords[1] + irinaButton.height
            ) {
                // Ambas imágenes están en las posiciones correctas
                // Realiza un Intent hacia Game2_Win4
                val intent = Intent(this, Game2_Win4::class.java)
                startActivity(intent)
                finish()  // Si deseas cerrar la actividad actual
            } else {
                // Puedes manejar aquí el caso en el que las imágenes no estén en las posiciones correctas
            }
        }
    }
}
