package com.azaldev.garden.games.ontziola

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities

class Game6_Win3 : AppCompatActivity() {
    private var corrects = 0
    private var wordImageMap: Map<Int, Pair<String, Int>> = mapOf(
        0 to Pair("txalupa", R.drawable.txalupa),
        1 to Pair("gurpil-lema", R.drawable.gurpillema),
        2 to Pair("masta", R.drawable.masta),
        3 to Pair("kofa", R.drawable.kofa),
        4 to Pair("belak", R.drawable.belak)
    )
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game6_win3)

        val game_id = 6
        val database = AppDatabase.getInstance(this)
        val gameDao = database.GameDao()
        val itsasontzia = findViewById<ImageView>(R.id.itsasontzia)

        val (initialWord, initialImage) = wordImageMap[corrects] ?: Pair("", 0)
        var wordToCompare = initialWord
        var imageToShow = initialImage

        itsasontzia.setImageResource(imageToShow)

        val editText = findViewById<EditText>(R.id.editTextText5)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Este método se llama para notificar cuando el texto está a punto de cambiar
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Este método se llama para notificar cuando el texto cambia
                val newText = s.toString().toLowerCase()

                if (newText == wordToCompare) {
                    hideKeyboard() // Oculta el teclado
                    Utilities.showToast(this@Game6_Win3, "Ondo egin duzu!! Hurrengoa...")
                    corrects++
                    handler.postDelayed({
                        val (newWord, newImage) = wordImageMap[corrects] ?: Pair("", 0)
                        wordToCompare = newWord
                        imageToShow = newImage
                        itsasontzia.setImageResource(imageToShow)

                        // Verifica si corrects alcanza 5 y inicia la nueva actividad
                        if (corrects == 5) {
                            Utilities.startActivity(this@Game6_Win3, Game6_Win4::class.java)
                            finish()  // Opcional: cierra la actividad actual si deseas
                        }
                    }, 1000)
                }
                // Log.i("devl|Game6_Win3", "New Text: $newText")
                // Puedes realizar acciones basadas en el nuevo texto aquí
            }

            override fun afterTextChanged(s: Editable?) {
                // Este método se llama para notificar después de que el texto ha cambiado
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
