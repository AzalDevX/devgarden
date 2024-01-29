package com.azaldev.garden.games.aldehistorikoa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.globals.Utilities

class Game3_Win4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_win4)
        val next_game = findViewById<Button>(R.id.next_game)


        next_game.setOnClickListener {
            Utilities.startActivity(this, LandingActivity::class.java)
        }

    }
}