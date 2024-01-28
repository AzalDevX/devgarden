package com.azaldev.garden.games.aldehistorikoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities

class Game3_Win2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_win2)

        val player_button = findViewById<ImageButton>(R.id.player_button)
        val next_game = findViewById<Button>(R.id.next_game)

        next_game.visibility = View.INVISIBLE

        player_button.setOnClickListener {
            Utilities.playSound(this,R.raw.game3_win2)
            player_button.visibility = View.INVISIBLE
            next_game.visibility = View.VISIBLE
        }

        next_game.setOnClickListener {
            Utilities.startActivity(this, Game3_Win3::class.java)
        }

    }
}