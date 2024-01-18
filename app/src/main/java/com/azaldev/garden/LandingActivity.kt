package com.azaldev.garden

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.globals.Globals
import com.azaldev.garden.globals.LocationServiceManager.startLocationService
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LandingActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase;
    private lateinit var gameDao: GameDao;
    private var cacheStoredUser: Auth? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        /**
         * Basically if there is a user logged we consider
         * them a teacher, so they will bypass game restrictions
         */
        if (Globals.stored_user == null)
            startLocationService(this, this, this);
        cacheStoredUser = Globals.stored_user;

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
         * Subscribe to out location update it there isn't any user logged
         */
        if (Globals.stored_user == null) {
            val filter = IntentFilter("com.azaldev.garden.LOCATION_UPDATE")
            registerReceiver(locationReceiver, filter)
        }

        loadGames(0.0,0.0, Globals.stored_user != null);

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            Utilities.startActivity(this, SettingsActivity::class.java);
        }
        findViewById<ImageButton>(R.id.maps_button).setOnClickListener {
            Utilities.startActivity(this, MapsActivity::class.java);
        }
    }

    fun loadGames(x: Double = 0.0, y: Double = 0.0, bypass: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val gameList = gameDao.getGames()

            lifecycleScope.launch(Dispatchers.Main) {
                val mainLayout = findViewById<LinearLayout>(R.id.card_container)

                mainLayout.removeAllViews()
                var is_last_unlocked = false;

                for (game in gameList) {
                    val customCardView = layoutInflater.inflate(R.layout.game_layout_template, null)
                    val isInRadious = Utilities.isLocationWithinRadius(x, y, game.x, game.y, 20f)

                    // Set image, title, and other details based on the game
                    val imageView = customCardView.findViewById<ImageView>(R.id.card_image)
                    val imageDisabledFilter = customCardView.findViewById<ImageView>(R.id.card_disabled)
                    val imageDisabledLock = customCardView.findViewById<ImageView>(R.id.card_locked)

                    val imageLocate = customCardView.findViewById<ImageView>(R.id.card_locate)
                    val titleTextView = customCardView.findViewById<TextView>(R.id.card_title)

                    val jumpAnimator = ObjectAnimator.ofFloat(imageLocate, "translationY", 0f, -25f, 0f)
                    jumpAnimator.duration = 1500 // Adjust the duration as needed
                    jumpAnimator.interpolator = AccelerateDecelerateInterpolator()
                    jumpAnimator.repeatCount = ObjectAnimator.INFINITE // Repeat the animation infinitely

                    jumpAnimator.start()

                    if (!is_last_unlocked) {
                        val swingAnimator = ObjectAnimator.ofFloat(imageDisabledLock, "rotation", 0f, 15f, 0f, -15f, 0f)
                        swingAnimator.duration = 1000 // Adjust the duration as needed
                        swingAnimator.interpolator = AccelerateDecelerateInterpolator()
                        swingAnimator.repeatCount = ObjectAnimator.INFINITE // Repeat the animation infinitely

                        swingAnimator.start()
                    } else {
                        val colorMatrix = ColorMatrix()
                        colorMatrix.setSaturation(0f)
                        val colorFilter = ColorMatrixColorFilter(colorMatrix)
                        imageDisabledLock.colorFilter = colorFilter
                    }

                    // Set image using game data
                    imageView.setImageResource(game.image)

                    // Set title using game data
                    titleTextView.text = game.name
                    imageDisabledFilter.visibility = if (game.isLocked && !bypass) View.VISIBLE else View.INVISIBLE
                    imageDisabledLock.visibility = if (game.isLocked && !bypass) View.VISIBLE else View.INVISIBLE
                    imageLocate.visibility = if (!isInRadious && !game.isLocked && !bypass) View.VISIBLE else View.INVISIBLE

                    imageLocate.setOnClickListener {
                        if (!isInRadious && !game.isLocked && !bypass)
                            Utilities.openGoogleMapsWithDirections(this@LandingActivity, game.x, game.y)
                    }

                    imageView.setOnClickListener {
                        if ((isInRadious && !game.isLocked) || bypass){
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
                        if(game.id == gameList.size)
                            resources.getDimensionPixelSize(R.dimen.card_margin) * 4
                        else 0   // bottom margin, we set it for the last card :D
                    )

                    // Apply layout parameters to customCardView
                    customCardView.layoutParams = layoutParams
                    is_last_unlocked = game.isLocked

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

    /**
     * Re-create the window if the user is not the same as the chached one
     */
    override fun onResume() {
        super.onResume()
        if (cacheStoredUser != Globals.stored_user)
            recreate();
        Log.i("devl|landing", "onResume() has been called, user is ${if (cacheStoredUser != Globals.stored_user) "changed" else "cached"}")
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