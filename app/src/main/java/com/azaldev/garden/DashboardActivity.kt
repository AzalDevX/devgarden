package com.azaldev.garden

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var logout_button: ImageButton
    private lateinit var back_button: ImageButton
    private lateinit var table_layout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        table_layout = findViewById(R.id.tableLayout)
        val rowCount = table_layout.childCount

        for (i in rowCount - 1 downTo 1) {
            val row: View = table_layout.getChildAt(i)
            if (row is TableRow) {
                table_layout.removeViewAt(i)
            }
        }

        data class TeamData(
            val name: String,
            val minigames: Int,
            val progress: String
        )

        val teamsData = arrayOf(
            TeamData("arrano-beltza", 4, "2/6"),
            TeamData("katu-urdina", 4, "3/6"),
            TeamData("txakur-gorria", 4, "5/6"),
            TeamData("bale-zuria", 4, "3/6")
        )

        for (team in teamsData) {
            createNewTeam(team.name, team.minigames, team.progress)
        }


        database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao()
        gameDao = database.GameDao()

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

    fun createNewTeam(team_name : String, team_minigame : Int, team_progress : String){
        val tableRow = TableRow(this)

        val teamNameTextView = createCenteredTextView(team_name,R.color.beige_700)
        val minigameTextView = createCenteredTextView(team_minigame.toString(),R.color.beige_700)
        val progressTextView = createCenteredTextView(team_progress,R.color.beige_700)

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
        return  centeredTextView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE;
        mMap.setMinZoomPreference(16f)
        lifecycleScope.launch(Dispatchers.IO) {
            val gameList = gameDao.getGames()

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
    }
}