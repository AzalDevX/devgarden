package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.*
import com.google.android.material.snackbar.Snackbar
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


        Log.d("devl|main", "Initializing classes...")


        if (device_lang != null) {
            Utilities.setLocale(this@MainActivity, device_lang)
            Log.i("devl|main", "App language has been restored from memory. current language \"${device_lang}\"")
        }

        val globals = Globals()

        Utilities.hasInternetConnection(this) { isConnected ->
            globals.has_connection = isConnected
            if (isConnected) {
                globals.webSocketClient = WSClient("https://socko.azaldev.com")
            }
        }

        Log.d("devl|main", "Finished initializing classes.")

        val gm = GameManager(this, this);
        gm.initializeGames();

        Log.d("devl|main", "Finished initializing games.")


        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                TimeUnit.SECONDS.sleep(2)
                Log.d("devl|main", "Launching landing page...")
//                Utilities.startActivity(this@MainActivity, LandingActivity::class.java)
                startActivity(Intent(this@MainActivity, LandingActivity::class.java))
            }
        }

        Log.d("devl|main", "Execution finished.")
    }
}