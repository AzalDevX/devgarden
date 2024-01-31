package com.azaldev.garden

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import com.google.gson.Gson
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.databinding.ActivityMapsBinding
import com.azaldev.garden.globals.Globals
import com.azaldev.garden.globals.Utilities
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

data class Location(
    val x: Double,
    val y: Double,
    val _id: String
)

data class Student(
    val _id: String,
    val name: String,
    val teacher_code: String,
    val location: Location,
    val progress: Int,
    val __v: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val all_students: List<Student>
)

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap           : GoogleMap
    private lateinit var database       : AppDatabase
    private lateinit var gameDao        : GameDao
    private lateinit var logout_button  : ImageButton
    private lateinit var back_button    : ImageButton
    private lateinit var table_layout   : TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        table_layout = findViewById(R.id.tableLayout)
        val rowCount = table_layout.childCount

        Log.i("devl|dashboard", "row count: $rowCount")

        database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao()
        gameDao = database.GameDao()

        logout_button = findViewById(R.id.logout_button)
        back_button = findViewById(R.id.back_button)

        val originalBitmap = BitmapFactory.decodeResource(resources,R.drawable.iconsbabyfeet)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
        val icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

        val contextView = findViewById<View>(R.id.dashLayoutCtx)
        Utilities.canConnectToApi {
            Globals.has_connection = it

            if (Globals.has_connection && Globals.webSocketClient == null)
                Globals.webSocketClient = WSClient(Globals.api_url)

            Log.i("devl|dashboard", "Internet connection status: $it, WSClient status: ${Globals.webSocketClient != null}")

            if (!Globals.has_connection) {
                Snackbar.make(contextView, "You are not connected to the internet, You wont have access to cloud features", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Recheck") {}
                    .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                    .show()
            }
        }

        val group_object = mapOf("token" to Utilities.sha256(Globals.stored_user?.email.toString()))

        Globals.webSocketClient?.emit("fetch_class", group_object)
        Globals.webSocketClient?.on("fetch_class") { data ->
            Log.i("devl|dashboard", data.toString())

            val res: ApiResponse = Gson().fromJson(data, ApiResponse::class.java)

            if (res.success && res.all_students.size > 0) {
                runOnUiThread {
                    for (i in rowCount - 1 downTo 1) {
                        val row: View = table_layout.getChildAt(i)
                        if (row is TableRow) {
                            table_layout.removeViewAt(i)
                        }

                    }

                    for (student in res.all_students) {
                        Log.i("devl|dashboard", "progress: ${student.progress}")
                        val student_progress =
                            if(student.progress >= 100)
                                student.progress.toString().toCharArray()[1] + "/" + student.progress.toString().toCharArray()[2]
                            else
                                "0/0"

                        createNewTeam(student.name, student.progress / 100, student_progress, student.location)

                        mMap.addMarker(
                            MarkerOptions()
                                .icon(icon)
                                .position(LatLng(student.location.x, student.location.y))
                                .title(student.name)

                        )

                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(student.location.x, student.location.y), 17f
                            )
                        )
                    }
                }
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        logout_button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                authDao.delete()
                Globals.stored_user = null
                finish()
            }
        }

        back_button.setOnClickListener {
            finish()
        }
    }

    fun createNewTeam(team_name : String, team_minigame : Int, team_progress : String, loc : Location){
        val tableRow = TableRow(this)

        val teamNameTextView = createCenteredTextView(team_name,R.color.beige_700)
        val minigameTextView = createCenteredTextView(team_minigame.toString(),R.color.beige_700)
        val progressTextView = createCenteredTextView(team_progress,R.color.beige_700)

        teamNameTextView.setOnClickListener {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.x, loc.y), 17f
                )
            )
        }

        tableRow.addView(teamNameTextView)
        tableRow.addView(minigameTextView)
        tableRow.addView(progressTextView)

        table_layout.addView(tableRow)
    }

    fun createCenteredTextView(data : String, color : Int) : TextView{
        val centeredTextView = TextView(this)
        centeredTextView.text = data
        centeredTextView.setTextColor(ContextCompat.getColor(this, color))
        centeredTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        centeredTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        return centeredTextView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap;

        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE;
        mMap.setMinZoomPreference(16f);

        /*
        lifecycleScope.launch(Dispatchers.IO) {
            // val gameList = gameDao.getGames()


            lifecycleScope.launch(Dispatchers.Main) {
                for (game in gameList) {
                    val originalBitmap = BitmapFactory.decodeResource(resources,R.drawable.iconsbabyfeet)
                    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
                    val icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

                    mMap.addMarker(
                        MarkerOptions()
                            .icon(icon)
                            .position(LatLng(game.x, game.y))
                            .title(game.name)

                    )
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(game.x, game.y), 17f
                        )
                    )

                }
            }
        }
         */
    }
}