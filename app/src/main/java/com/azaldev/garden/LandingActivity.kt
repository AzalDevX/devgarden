package com.azaldev.garden

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.LocationServiceManager.startLocationService
import com.azaldev.garden.globals.Utilities

class LandingActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        startLocationService(this, this);

        //Save on room database
        val database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao();
        //        db.GlobalSettingsDao().insertGlobalSettings()

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            Utilities.startActivity(this, SettingsActivity::class.java);
//            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.maps_button).setOnClickListener {
            Utilities.startActivity(this, MapsActivity::class.java);
//            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.location_1).setOnClickListener {
            openGoogleMapsWithDirections(this)
        }
    }

    private fun openGoogleMapsWithDirections(context: Context) {
        val destinationLat = 43.40257
        val destinationLon = -2.94652
        
        val gmmIntentUri = Uri.parse("google.navigation:q=$destinationLat,$destinationLon&mode=w")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        }
    }

}




    @SuppressLint("MissingSuperCall")
    fun onBackPressed() {
//        super.onBackPressed()
        /**
         * Block going back to the MainActivity
         */
    }
