package com.azaldev.garden.games.pasabidea

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities

data class ShipPoint(val x: Float, val y: Float, val order: Int)

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    private val dotRadius = 20f
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shipPoints = listOf(
        ShipPoint(100f, 600f, 1),
        ShipPoint(190f, 590f, 2),
        ShipPoint(280f, 580f, 3),
        ShipPoint(370f, 570f, 4),
        ShipPoint(460f, 560f, 5),
        ShipPoint(550f, 555f, 6),
        ShipPoint(640f, 560f, 7),
        ShipPoint(730f, 570f, 8),
        ShipPoint(820f, 580f, 9),
        ShipPoint(910f, 590f, 10),
        ShipPoint(1000f, 600f, 11),
        ShipPoint(1000f, 550f, 12),
        ShipPoint(910f, 490f, 13),
        ShipPoint(820f, 440f, 14),
        ShipPoint(730f, 390f, 15),
        ShipPoint(640f, 340f, 16),
        ShipPoint(550f, 320f, 17),
        ShipPoint(460f, 340f, 18),
        ShipPoint(370f, 390f, 19),
        ShipPoint(280f, 440f, 20),
        ShipPoint(190f, 490f, 21),
        ShipPoint(100f, 550f, 22),
        // Agrega más puntos según sea necesario
    ).map {
        val screenWidth = 2134f // Ancho de pantalla en modo landscape
        val screenHeight = 1080f // Suponiendo un alto de pantalla (puedes ajustarlo)

        val xRatio = it.x / screenWidth
        val yRatio = it.y / screenHeight

        val newX = xRatio * 2134 // Ajusta el valor según el nuevo ancho de pantalla
        val newY = yRatio * screenHeight // Mantiene la proporción del alto de pantalla

        ShipPoint(newX, newY, it.order)
    }

    private var touchedPoints = mutableListOf<ShipPoint>()

    init {
        dotPaint.color = resources.getColor(android.R.color.black)
        pathPaint.color = resources.getColor(android.R.color.black)
        pathPaint.strokeWidth = 10f
        pathPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (point in shipPoints) {
            canvas.drawCircle(point.x, point.y, dotRadius, dotPaint)
        }
        if (touchedPoints.size > 1) {
            for (i in 1 until touchedPoints.size) {
                val startPoint = touchedPoints[i - 1]
                val endPoint = touchedPoints[i]
                canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, pathPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchedPoint = findTouchedPoint(event.x, event.y)
                if (touchedPoint != null && !containsPointWithOrder(touchedPoint.order)) {

                    touchedPoints.add(touchedPoint)
                    Log.d("CustomView", "touchedPoints: $touchedPoints")
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val touchedPoint = findTouchedPoint(event.x, event.y)
                if (touchedPoint != null && !containsPointWithOrder(touchedPoint.order) && !touchedPoints.contains(touchedPoint)) {
                    touchedPoints.add(touchedPoint)
                }
            }
            MotionEvent.ACTION_UP -> {
                // Verifica si el usuario ha conectado los puntos en el orden correcto
                if (isCorrectOrder()) {
                    Log.d("CustomView", "TouchedPoints: fine")
                    // El usuario ha completado el barco correctamente
                    Utilities.playSound(context, R.raw.success, {})
                } else {
                    Log.d("CustomView", "TouchedPoints: not finished")


                        val isSequential = isSequentialOrder()
                        if (!isSequential) {
                            Log.d("CustomView", "MAL: no está en orden secuencial")
                            Utilities.playSound(context, R.raw.error, {})
                            handler.postDelayed({

                                Log.d("CustomView", "TouchedPoints: vuelve a empezar después del timeout")
                            }, 2000)
                            touchedPoints.clear()

                        }



                }
            }
        }
        invalidate()
        return true
    }

    private fun findTouchedPoint(x: Float, y: Float): ShipPoint? {
        // Verifica si el usuario tocó alguno de los puntos del barco
        for (point in shipPoints) {
            val distanceToTouch = Math.sqrt((x - point.x).toDouble() * (x - point.x).toDouble() + (y - point.y).toDouble() * (y - point.y).toDouble())
            if (distanceToTouch <= dotRadius) {
                return point
            }
        }
        return null
    }


    private fun checkAndAddTouchedPoint(x: Float, y: Float) {
        // Verifica si el usuario tocó alguno de los puntos del barco
        for (point in shipPoints) {
            val distanceToTouch = Math.sqrt((x - point.x).toDouble() * (x - point.x).toDouble() + (y - point.y).toDouble() * (y - point.y).toDouble())
            if (distanceToTouch <= dotRadius) {
                // Agrega el punto tocado a la lista
                touchedPoints.add(point)
                 // Quita este break para permitir que se acumulen todos los puntos tocados
            }
        }
    }

    private fun containsPointWithOrder(order: Int): Boolean {
        // Verifica si ya hay un punto en touchedPoints con el mismo orden
        return touchedPoints.any { it.order == order }
    }
    private fun isSequentialOrder(): Boolean {
        // Verifica si los puntos tocados están en orden secuencial
        return touchedPoints.zipWithNext { first, second -> first.order + 1 == second.order }.all { it }
    }

    fun isCorrectOrder(): Boolean {
        // Verifica si los puntos tocados están en el orden correcto
        Log.d("CustomView", "TouchedPoints: ${touchedPoints.size}")
        Log.d("CustomView", "ShipPoints: $shipPoints ")
        return touchedPoints.size == shipPoints.size &&
                touchedPoints.map { it.order } == touchedPoints.mapIndexed { index, _ -> index + 1 }
    }

}


