package com.azaldev.garden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Game
import com.azaldev.garden.globals.LocationServiceManager.startLocationService
import com.azaldev.garden.globals.Utilities

class LandingActivity : AppCompatActivity() {

    val gameList: List<Game> = listOf(
        Game(
            id = 1,
            coords = mapOf("x" to 43.40257, "y" to -2.94652),
            name = "Pasabidea",
            image = R.drawable.pasabidea,
            isLocked = false,
            isFinished = false
        ),
        Game(
            id = 2,
            coords = mapOf("x" to 43.4029, "y" to -2.94519),
            name = "Errota",
            image = R.drawable.errota,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 3,
            coords = mapOf("x" to 43.40469, "y" to -2.94762),
            name = "Alde Historikoa",
            image = R.drawable.zaharra,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 4,
            coords = mapOf("x" to 43.40519, "y" to -2.94778),
            name = "Magdalena Eliza",
            image = R.drawable.madalena,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 5,
            coords = mapOf("x" to 43.40545, "y" to -2.94772),
            name = "Santiago Arkua",
            image = R.drawable.arkua,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 6,
            coords = mapOf("x" to 43.40436, "y" to -2.94983),
            name = "Ontziola",
            image = R.drawable.plaza,
            isLocked = true,
            isFinished = true
        ),
        Game(
            id = 7,
            coords = mapOf("x" to 43.40739, "y" to -2.94522),
            name = "Portua/Hondartza",
            image = R.drawable.hondartza,
            isLocked = true,
            isFinished = true
        ),
    )

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        startLocationService(this, this);

//        val mapPointer: ImageView = findViewById(R.id.location_1)
//        val animation = AnimationUtils.loadAnimation(this, R.anim)
//        mapPointer.startAnimation(animation)

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
            Utilities.openGoogleMapsWithDirections(this, 43.40257, -2.94652)
        }
    }

//    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        /**
         * Block going back to the MainActivity
         */
        if (true == false)
            super.onBackPressed()
    }
}