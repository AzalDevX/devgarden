package com.azaldev.garden.games.aldehistorikoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities

class Game3_Win1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_win1)

        val player_button = findViewById<ImageButton>(R.id.player_button)
        val next_game = findViewById<Button>(R.id.next_game)

        next_game.visibility = INVISIBLE

        player_button.setOnClickListener {
            Utilities.playSound(this,R.raw.game3_win1)
            player_button.visibility = INVISIBLE
            next_game.visibility = VISIBLE
        }

        next_game.setOnClickListener {
            Utilities.startActivity(this, Game3_Win2::class.java)
        }

    }
}