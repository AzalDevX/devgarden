package com.azaldev.garden

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var logout_button: ImageButton
    private lateinit var back_button: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao();
        gameDao = database.GameDao();

        logout_button = findViewById(R.id.logout_button)
        back_button = findViewById(R.id.back_button)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        logout_button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                authDao.delete()
                finish()
            }
        }

        back_button.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE;
        mMap.setMinZoomPreference(16f)
        lifecycleScope.launch(Dispatchers.IO) {
            val gameList = gameDao.getGames();

            lifecycleScope.launch(Dispatchers.Main) {
                for (game in gameList) {
                    val originalBitmap = BitmapFactory.decodeResource(resources, if (game.id % 2 == 0) R.drawable.birdleft else R.drawable.birdright)
                    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
                    val icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

                    mMap.addMarker(
                        MarkerOptions()
                            .icon(icon)
                            .position(LatLng(game.x, game.y))
                            .title(game.name)

                    )

                    if (!game.isLocked)
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(game.x, game.y),
                                17f)
                        )

                }
            }
        }
    }
}