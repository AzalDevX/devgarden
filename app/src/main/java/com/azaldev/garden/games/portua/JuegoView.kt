package com.azaldev.garden.games.portua

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.azaldev.garden.R

class JuegoView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val barco: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.itsasontziatxikia)
    private val roca: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.arroka)

    private var barcoX: Float = 0f
    private var rocaX: Float = 0f
    private var rocaY: Float = 0f
    private val ESCALA_BARCO = 1.0f

    private val carriles: Int = 3
    private var carrilSeleccionado: Int = 1

    private val handler = Handler(Looper.getMainLooper())
    private var juegoIniciado: Boolean = false
    private var tiempoRestante: Int = 30

    init {
        // No necesitamos inicializar barcoX aquí
        // Inicializar la posición inicial de la roca
        resetRoca()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Establecer la posición inicial del barco después de conocer el ancho de la vista
        barcoX = (w - barco.width * ESCALA_BARCO) / 2f
    }

    private fun resetRoca() {
        carrilSeleccionado = (1..carriles).random()
        rocaX = (carrilSeleccionado - 1) * (width.toFloat() / carriles)
        rocaY = -roca.height.toFloat()
    }

    private fun moverRoca() {
        rocaY += 30 // Velocidad de movimiento de la roca

        // Verificar colisión con el barco
        if (rocaY + roca.height >= height - barco.height && rocaX + roca.width >= barcoX && rocaX <= barcoX + barco.width) {
            // ¡Colisión! El jugador ha perdido.
            resetRoca()
            detenerJuego()
            mostrarMensajePerdido()

        }

        // Verificar si la roca ha alcanzado la parte inferior de la pantalla
        if (rocaY >= height) {
            // La roca ha llegado al fondo, reiniciar su posición
            resetRoca()

        }
    }
    private fun mostrarMensajePerdido() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("¡Has perdido!")
            .setMessage("Inténtalo de nuevo")
            .setPositiveButton("Aceptar") { _, _ ->
                // Reiniciar el juego después de aceptar el mensaje
                reiniciarJuego()
            }
            .setCancelable(false) // Evitar que se cierre al tocar fuera del diálogo
            .show()
    }
    private fun mostrarMensajeGanado() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Zorionak!")
            .setMessage("Irabazi duzu minijoku guztiak")
            .setPositiveButton("Aceptar") { _, _ ->


            }
            .setCancelable(false) // Evitar que se cierre al tocar fuera del diálogo
            .show()
    }

    private fun iniciarJuego() {
        juegoIniciado = true
        tiempoRestante = 30
        iniciarContador()
        resetRoca()

        handler.post(object : Runnable {
            override fun run() {
                if (juegoIniciado) {
                    moverRoca()
                    invalidate()
                    handler.postDelayed(this, 16) // Actualizar cada 16 milisegundos
                }
            }
        })
    }


    private fun iniciarContador() {
        handler.post(object : Runnable {
            override fun run() {
                if (tiempoRestante > 0) {
                    tiempoRestante--
                    handler.postDelayed(this, 1000) // Actualizar cada segundo
                } else {
                    // Juego terminado
                    mostrarMensajeGanado()
                    juegoIniciado = false
                    // Hacer visible el botón cuando el contador termine
                    (context as? AppCompatActivity)?.runOnUiThread {
                        (context as? AppCompatActivity)?.findViewById<Button>(R.id.next_game)?.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun detenerJuego() {
        juegoIniciado = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Verificar si el ancho y la altura de la vista son mayores que 0 antes de dibujar
        if (width > 0 && height > 0) {
            // Calcular la posición ajustada del barco teniendo en cuenta la escala
            val scaledBarcoX = barcoX * ESCALA_BARCO
            val scaledBarcoY = height - barco.height * ESCALA_BARCO

            // Dibujar el barco en la parte inferior de la pantalla con la escala aplicada
            canvas.drawBitmap(barco, scaledBarcoX, scaledBarcoY, Paint())

            // Dibujar la roca en su posición actual
            canvas.drawBitmap(roca, rocaX, rocaY, Paint())
        }
    }

    // Manejar el evento de toque para mover el barco
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_MOVE -> {
                // Mover el barco horizontalmente con el dedo
                barcoX = event.x - barco.width / 2
                // Asegurarse de que el barco no se salga de la pantalla
                barcoX = barcoX.coerceIn(0f, width - barco.width.toFloat())
                invalidate() // Redibujar la vista
            }
        }
        return true
    }

    fun reiniciarJuego() {
        detenerJuego()
        iniciarJuego()
    }
}