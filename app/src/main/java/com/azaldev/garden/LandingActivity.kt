package com.azaldev.garden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.azaldev.garden.classes.database.AppDatabase

class LandingActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        //Generate random name

        //Save on room database
        db = AppDatabase.getInstance(this)
        db.GlobalSettingsDao().insertGlobalSettings()


    }
}