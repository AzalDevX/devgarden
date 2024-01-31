package com.azaldev.garden

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.azaldev.garden.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database: AppDatabase;
    private lateinit var gameDao: GameDao;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getInstance(applicationContext)
        gameDao = database.GameDao();

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE;
        mMap.setMinZoomPreference(15f)
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