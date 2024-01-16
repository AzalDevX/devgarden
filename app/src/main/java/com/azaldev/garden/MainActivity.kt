package com.azaldev.garden

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.GameManager
import com.azaldev.garden.globals.Globals
import com.azaldev.garden.globals.PermissionUtils
import com.azaldev.garden.globals.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )


        val device_lang = Locale.getDefault().language
        Log.i("devl|startup", "App has been started, device information:")
        Log.i("devl|model", "Device Model: ${android.os.Build.MODEL}")
        Log.i("devl|language", "Device Language: ${device_lang}")
        Log.i("devl|version", "Android Version: ${android.os.Build.VERSION.RELEASE}")
        Log.i("devl|screen", "Screen Size: ${resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK}")
        Log.i("devl|resolution", "Screen Resolution: ${resources.displayMetrics.widthPixels.toString() + "x" + resources.displayMetrics.heightPixels.toString()}")

        Log.d("devl|main", "MainActivity has started!")


        Log.i("devl|main", "Initializing classes...")

        Utilities.canConnectToApi {
            Globals.has_connection = it

            if (Globals.has_connection)
                Globals.webSocketClient = WSClient(Globals.api_url)

            Log.i("devl|main", "Internet connection status: $it, WSClient status: ${Globals.webSocketClient != null}")
        }

        Log.d("devl|main", "Finished initializing classes.")

        Log.i("devl|main", "Initializing database and settings...")

        val database = AppDatabase.getInstance(applicationContext);
        var authDao = database.AuthDao()
        var settinsDao = database.GlobalSettingsDao()
        val updateText = findViewById<TextView>(R.id.introProcessDetails)

        lifecycleScope.launch(Dispatchers.IO) {
            updateText.text = "Initializing database..."
            try {
                Globals.stored_user = authDao.get()
                Globals.stored_settings = settinsDao.getDefault()
            } catch (e: IllegalStateException) {
                withContext(Dispatchers.Main) {
                    Utilities.showErrorAlert(this@MainActivity, "Database schema mismatch. Please update the app.") {
                        finish()
                    }
                }
                return@launch
            }

            Log.i("devl|initializer", "Initializing database waiting for a successfully response");

            var locale = Globals.stored_settings?.lang ?: device_lang
            Utilities.setLocale(this@MainActivity, locale)
            Log.i("devl|main", "App language has been restored from memory. locale set to $locale")

            Log.i("devl|main", "Initializing games...")

            val gm = GameManager(this@MainActivity, this@MainActivity);
            gm.initializeGames();

            Log.d("devl|main", "Finished initializing games.")


            Log.i("devl|main", "Requesting essential permissions...")

            updateText.text = "Requesting permissions..."

            var loop_check = 0
            do {
                val locationPermission = PermissionUtils.checkAndRequestLocationPermission(this@MainActivity)

                Log.i("devl|main", "Location permission: $locationPermission")
                // val cameraPermission = PermissionUtils.checkAndRequestCameraPermission(this@MainActivity)

                if (locationPermission)
                    break

                if (loop_check == 5) {
                    withContext(Dispatchers.Main) {

                        val imageView = ImageView(this@MainActivity)
                        imageView.setImageResource(R.drawable.permissionimage)
                        imageView.adjustViewBounds = true

                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Permissions required")
                            .setMessage("You need to grant permissions to use the app.")
                            .setView(imageView)
                            .setNeutralButton("Cancel") { dialog, which ->
                                Utilities.showToast(this@MainActivity, "You need to grant permissions to use the app.")
                                finish()
                            }
                            .setNegativeButton("Decline") { dialog, which ->
                                Utilities.showToast(this@MainActivity, "You need to grant permissions to use the app.")
                                finish()
                            }
                            .setPositiveButton("Accept") { dialog, which ->
                                Utilities.showToast(this@MainActivity, "Opening settings...")

                                val packageName = "com.azaldev.garden"
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)

                                finish()
                            }
                            .show()
                    }
                }

                TimeUnit.SECONDS.sleep(2)
                loop_check++;
            } while (loop_check <= 10)

            if (loop_check >= 10) {
                withContext(Dispatchers.Main) {
                    Utilities.showToast(this@MainActivity, "You need to grant permissions to use the app.")
                    finish()
                }
                return@launch
            }

            updateText.text = "Permissions granted, launching landing page..."

            TimeUnit.SECONDS.sleep(1)
            Log.d("devl|main", "Launching landing page...")
            startActivity(Intent(this@MainActivity, LandingActivity::class.java))
        }

        Log.d("devl|main", "Execution finished.")
    }

    /*
    override fun onResume() {
        super.onResume()

        val locationPermission = PermissionUtils.checkAndRequestLocationPermission(this@MainActivity)

        Log.i("devl|main", "onResume() Location permission: $locationPermission")

        if (
            locationPermission
        ) {
            TimeUnit.SECONDS.sleep(1)
            Log.d("devl|main", "Launching landing page...")
            startActivity(Intent(this@MainActivity, LandingActivity::class.java))
        }
    }
    */
}