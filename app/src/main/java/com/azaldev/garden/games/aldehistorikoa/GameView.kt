package com.azaldev.garden.games.aldehistorikoa

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import com.google.zxing.common.detector.MathUtils.distance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val linePaint: Paint = Paint()
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var endX: Float = 0f
    private var endY: Float = 0f
    private var counter: Int = 0
    private var imageStart: String = ""
    private var imageEnd: String = ""
    private lateinit var activityContext: Context  // Nuevo campo para almacenar el contexto de la actividad

    // Nuevas variables para almacenar los umbrales de distancia
    private var startDistanceThreshold: Float = 0f
    private var endDistanceThreshold: Float = 0f
    private val game_id = 3
    private lateinit var database : AppDatabase
    private lateinit var gameDao : GameDao


    init {
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5f
        linePaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(startX, startY, endX, endY, linePaint)
    }

    fun clearCanvas() {
        // Restaura las coordenadas de inicio y fin a cero para que la línea desaparezca
        startX = 0f
        startY = 0f
        endX = 0f
        endY = 0f

        // Invalida la vista para que se llame a onDraw y se borre la línea
        invalidate()
    }

    fun handleTouchEvent(action: Int, x: Float, y: Float) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                endX = startX
                endY = startY
                // Inicializa los umbrales de distancia al principio del dibujo
                startDistanceThreshold = 100f
                endDistanceThreshold = 100f
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                endX = x
                endY = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // Obtén las IDs de las imágenes más cercanas a la línea y muéstralas en el Log
                val closestImageIds = findClosestImagesToLine()
                val closestStartImageIds = findClosestStartImagesToLine()

                if (closestStartImageIds.isNotEmpty() && closestImageIds.isNotEmpty()) {
                    imageStart = getResourceNameWithoutPackage(closestStartImageIds[0])
                    imageEnd = getResourceNameWithoutPackage(closestImageIds[0])

                    val correctSolutions = listOf(
                        Pair("bakailaoa", "barren"),
                        Pair("burdin", "kristo"),
                        Pair("sardina", "eliza")
                    )

                    // Verifica si ambas IDs coinciden con alguna solución correcta
                    if (correctSolutions.any { it.first == imageStart && it.second == imageEnd }) {
                        Log.i("devl|GameView", "Coincidencia encontrada: ($imageStart, $imageEnd)")
                        counter++
                        Utilities.showToast(activityContext,"$counter/3")
                        hideClosestImages()
                        clearCanvas()
                        if (counter >= 3) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val current_game = gameDao.getGame(game_id);

                                val next_game = current_game.getActivityProgress(1)
                                gameDao.adv_progress(game_id, 1);

                                Log.i("devl|game33", "Moving to the next game")
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (next_game != null)
                                        Utilities.startActivity(activityContext, next_game)
                                    else
                                        Utilities.startActivity(activityContext, LandingActivity::class.java)
                                }
                            }
                        }
                    } else {
                        Log.i("devl|GameView", "No hay coincidencia con las IDs: ($imageStart, $imageEnd)")
                    }
                }
            }
        }
    }

    // Nuevo método para inicializar el contexto de la actividad
    fun initActivityContext(activityContext: Context) {
        this.activityContext = activityContext
        this.database = AppDatabase.getInstance(activityContext)
        this.gameDao = database.GameDao();
    }

    private fun hideClosestImages() {
        val closestStartImageIds = findClosestStartImagesToLine()
        val closestEndImageIds = findClosestImagesToLine()

        // Oculta las imágenes cercanas
        closestStartImageIds.forEach { hideImageById(it) }
        closestEndImageIds.forEach { hideImageById(it) }
    }

    private fun hideImageById(imageId: Int) {
        val imageView = (context as? Game3_Win3)?.findViewById<ImageView>(imageId)
        imageView?.visibility = View.INVISIBLE
    }

    private fun getResourceNameWithoutPackage(resourceId: Int): String {
        val resourceName = resources.getResourceName(resourceId)
        return resourceName.substringAfterLast("/")
    }

    private fun checkIfLinkIsCorrect(
        imageCoordinates: Pair<Float, Float>,
        lineEndCoordinates: Pair<Float, Float>,
        lineStartCoordinates: Pair<Float, Float>
    ): Boolean {
        // Ejemplo simple: verifica si las coordenadas de la línea están cerca del centro del segmento
        val centerX = (imageCoordinates.first + lineEndCoordinates.first) / 2
        val centerY = (imageCoordinates.second + lineEndCoordinates.second) / 2

        return (
                distance(centerX, centerY, lineStartCoordinates.first, lineStartCoordinates.second) < startDistanceThreshold ||
                        distance(centerX, centerY, lineEndCoordinates.first, lineEndCoordinates.second) < endDistanceThreshold
                )
    }

    private fun findClosestImagesToLine(): List<Int> {
        val imageIds = listOf(
            R.id.eliza, R.id.burdin, R.id.bakailaoa,
            R.id.sardina, R.id.kristo, R.id.barren
        )

        val closestImageIds = mutableListOf<Int>()

        for (imageId in imageIds) {
            val imageCoordinates = getImageCoordinates(imageId)
            val isClose = checkIfLinkIsCorrect(
                Pair(imageCoordinates.first, imageCoordinates.second),
                Pair(endX, endY),
                Pair(startX, startY)
            )

            if (isClose) {
                closestImageIds.add(imageId)
            }
        }

        return closestImageIds
    }

    private fun findClosestStartImagesToLine(): List<Int> {
        val imageIds = listOf(
            R.id.eliza, R.id.burdin, R.id.bakailaoa,
            R.id.sardina, R.id.kristo, R.id.barren
        )

        val closestImageIds = mutableListOf<Int>()

        for (imageId in imageIds) {
            val imageCoordinates = getImageCoordinates(imageId)
            val isClose = checkIfLinkIsCorrect(
                Pair(imageCoordinates.first, imageCoordinates.second),
                Pair(startX, startY),
                Pair(endX, endY)
            )

            if (isClose) {
                closestImageIds.add(imageId)
            }
        }

        return closestImageIds
    }

    private fun getImageCoordinates(imageId: Int): Pair<Float, Float> {
        val imageView = (context as? Game3_Win3)?.findViewById<ImageView>(imageId)

        // Asegúrate de que imageView no sea nulo antes de acceder a sus propiedades
        if (imageView != null) {
            return Pair(
                imageView.x + imageView.width / 2,
                imageView.y + imageView.height / 2
            )
        } else {
            // Manejar el caso en que la referencia a la ImageView es nula
            return Pair(0f, 0f)  // Puedes ajustar este valor según sea necesario
        }
    }
}
