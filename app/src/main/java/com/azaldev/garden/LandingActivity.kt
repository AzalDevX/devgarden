package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities

class LandingActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        //Generate random name

        //Save on room database
        val database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao();
        //        db.GlobalSettingsDao().insertGlobalSettings()

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            Utilities.startActivity(this, SettingsActivity::class.java);
        }

    }
}