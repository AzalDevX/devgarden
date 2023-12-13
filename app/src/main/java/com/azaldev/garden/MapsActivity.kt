package com.azaldev.garden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.azaldev.garden.databinding.ActivityMapsBinding
import com.azaldev.garden.globals.Utilities
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Add a marker in Sydney and move the camera
        val plentziakoPasabidea = LatLng(43.40257, -2.94652)
        val plentziakoErrota = LatLng(43.40297, -2.94519)
        val plentziakoAldeHistorikoa = LatLng(43.40469, -2.94762)
        val plentziakoSantaMariaMagdalenarenEliza = LatLng(43.40519, -2.94778)
        val plentziakoSantiagoArkua = LatLng(43.40545, -2.94772)
        val plentziakoOntziolarenEnparantza = LatLng(43.40436, -2.94983)
        val plentziakoPortuaHondartza = LatLng(43.40739, -2.94522)
        mMap.addMarker(MarkerOptions().position(plentziakoPasabidea).title("Pasabidea"))
        mMap.addMarker(MarkerOptions().position(plentziakoErrota).title("Errota"))
        mMap.addMarker(MarkerOptions().position(plentziakoAldeHistorikoa).title("Alde Historikoa"))
        mMap.addMarker(MarkerOptions().position(plentziakoSantaMariaMagdalenarenEliza).title("Eliza"))
        mMap.addMarker(MarkerOptions().position(plentziakoSantiagoArkua).title("Arkua"))
        mMap.addMarker(MarkerOptions().position(plentziakoOntziolarenEnparantza).title("Enparantza"))
        mMap.addMarker(MarkerOptions().position(plentziakoPortuaHondartza).title("Hondartza"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(plentziakoPasabidea, 16f))


    }
}