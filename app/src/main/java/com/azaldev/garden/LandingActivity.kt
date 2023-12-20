package com.azaldev.garden

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Game
import com.azaldev.garden.globals.GameManager
import com.azaldev.garden.globals.LocationServiceManager.startLocationService
import com.azaldev.garden.globals.Utilities
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase;
    private lateinit var gameDao: GameDao;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        startLocationService(this, this, this);

        database = AppDatabase.getInstance(applicationContext)
        gameDao = database.GameDao();

        /**
         * Calculate the left padding based on the screen size and card size, ez tbh
         */
        val cardScroll = findViewById<ScrollView>(R.id.cardScrollView)
        val cardWidth = resources.getDimensionPixelSize(R.dimen.card_width)

        if (resources.displayMetrics.widthPixels > cardWidth)
            cardScroll.setPadding(
                (resources.displayMetrics.widthPixels - cardWidth) / 2,
                0, 0, 0)

        /**
         * Subscribe to out location update
         */
        val filter = IntentFilter("com.azaldev.garden.LOCATION_UPDATE")
        registerReceiver(locationReceiver, filter)

        loadGames(0.0,0.0);

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            Utilities.startActivity(this, SettingsActivity::class.java);
        }
        findViewById<ImageButton>(R.id.maps_button).setOnClickListener {
            Utilities.startActivity(this, MapsActivity::class.java);
        }
    }

    fun loadGames(x: Double, y: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val gameList = gameDao.getGames()

            lifecycleScope.launch(Dispatchers.Main) {

                val mainLayout = findViewById<LinearLayout>(R.id.card_container)

                mainLayout.removeAllViews()

                for (game in gameList) {
                    val customCardView = layoutInflater.inflate(R.layout.game_layout_template, null)
                    val isInRadious = Utilities.isLocationWithinRadius(x, y, game.x, game.y, 20f)

                    // Set image, title, and other details based on the game
                    val imageView = customCardView.findViewById<ImageView>(R.id.card_image)
                    val imageDisabledFilter = customCardView.findViewById<ImageView>(R.id.card_disabled)
                    val imageDisabledLock = customCardView.findViewById<ImageView>(R.id.card_locked)

                    val imageLocate = customCardView.findViewById<ImageView>(R.id.card_locate)
                    val titleTextView = customCardView.findViewById<TextView>(R.id.card_title)

                    // Set image using game data
                    imageView.setImageResource(game.image)

                    // Set title using game data
                    titleTextView.text = game.name
                    imageDisabledFilter.visibility = if (game.isLocked) View.VISIBLE else View.INVISIBLE
                    imageDisabledLock.visibility = if (game.isLocked) View.VISIBLE else View.INVISIBLE
                    imageLocate.visibility = if (!isInRadious && !game.isLocked) View.VISIBLE else View.INVISIBLE

                    imageLocate.setOnClickListener {
                        if (!isInRadious && !game.isLocked)
                            Utilities.openGoogleMapsWithDirections(this@LandingActivity, game.x, game.y)
                    }

                    imageView.setOnClickListener {
                        if (isInRadious && !game.isLocked){
                            val activityClass = game.getActivityClass()

                            if (activityClass != null) {
                                Utilities.startActivity(this@LandingActivity, activityClass)
                            } else {
                                Utilities.showToast(this@LandingActivity, "Game is not yet implemented...")
                            }
                        }
                    }

                    // Set layout parameters programmatically
                    val layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.card_width),
                        resources.getDimensionPixelSize(R.dimen.card_height)
                    )

                    // Set margins programmatically
                    layoutParams.setMargins(
                        0, // left margin
                        resources.getDimensionPixelSize(R.dimen.card_margin), // top margin
                        0, // right margin
                        0 // resources.getDimensionPixelSize(R.dimen.card_margin_vertical) // bottom margin
                    )

                    // Apply layout parameters to customCardView
                    customCardView.layoutParams = layoutParams

                    // Add card view to the main layout
                    mainLayout.addView(customCardView)
                }
            }
        }
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.azaldev.garden.LOCATION_UPDATE") {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)

                Log.i("devl|landing", "Location update has been triggered, new location x: $latitude y: $longitude")
                // Call loadGames with the updated location
                    loadGames(latitude, longitude)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        unregisterReceiver(locationReceiver)
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