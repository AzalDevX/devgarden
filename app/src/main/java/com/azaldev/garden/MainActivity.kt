package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.*
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

        Utilities.hasInternetConnection(this) { isConnected ->
            Globals.has_connection = isConnected
            if (isConnected) {
                Globals.webSocketClient = WSClient("https://socko.azaldev.com")
            }
        }

        Log.d("devl|main", "Finished initializing classes.")

        Log.i("devl|main", "Initializing database and settings...")

        val database = AppDatabase.getInstance(applicationContext);
        var authDao = database.AuthDao()
        var settinsDao = database.GlobalSettingsDao()

        lifecycleScope.launch(Dispatchers.IO) {
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

            var loop_check = 0
            while (
                !PermissionUtils.checkAndRequestCameraPermission(this@MainActivity) ||
                !PermissionUtils.checkAndRequestLocationPermission(this@MainActivity)
            ) {
                TimeUnit.SECONDS.sleep(2)
                loop_check += 1
                if (loop_check > 5) {
                    withContext(Dispatchers.Main) {
                        val imageView = ImageView(this@MainActivity)
                        imageView.setImageResource(R.drawable.permissionimage)
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Permissions required")
                            .setMessage("You need to grant permissions to use the app.")
                            .setView(imageView)
                            .setNeutralButton("Cancel") { dialog, which ->
                                Utilities.showToast(this@MainActivity, "You need to grant permissions to use the app.")
                            }
                            .setNegativeButton("Decline") { dialog, which ->
                                Utilities.showToast(this@MainActivity, "You need to grant permissions to use the app.")
                            }
                            .setPositiveButton("Accept") { dialog, which ->
                               Utilities.showToast(this@MainActivity, "Opening settings...")
                            }
                            .show()
                    }
                    return@launch
                }
            }

            TimeUnit.SECONDS.sleep(1)
            Log.d("devl|main", "Launching landing page...")
            startActivity(Intent(this@MainActivity, LandingActivity::class.java))
        }

        Log.d("devl|main", "Execution finished.")
    }
}