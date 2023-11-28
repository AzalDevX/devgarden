package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.azaldev.garden.classes.database.AppDatabase

class LandingActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        //Generate random name

        //Save on room database
        db = AppDatabase.getInstance(this)
//        db.GlobalSettingsDao().insertGlobalSettings()

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

    }
}