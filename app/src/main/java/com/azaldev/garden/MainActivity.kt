package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.azaldev.garden.globals.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val device_lang = Locale.getDefault().language
        Log.i("devl|startup", "App has been started, device information:")
        Log.i("devl|model", "Device Model: ${android.os.Build.MODEL}")
        Log.i("devl|language", "Device Language: ${device_lang}")
        Log.i("devl|version", "Android Version: ${android.os.Build.VERSION.RELEASE}")
        Log.i("devl|screen", "Screen Size: ${resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK}")
        Log.i("devl|resolution", "Screen Resolution: ${resources.displayMetrics.widthPixels.toString() + "x" + resources.displayMetrics.heightPixels.toString()}")
+
        Log.d("devl|main", "MainActivity has started!")

        Log.d("devl|main", "Initializing classes...")

        Utilities.hasInternetConnection(this) { isConnected ->
            if (isConnected) {
                Utilities.showToast(this, "Internet is available")
            } else {
                Utilities.showToast(this, "No internet connection")
            }
        }

        val intent = Intent(this, LandingActivity::class.java)
        startActivity(intent)

        Log.d("devl|main", "Finished initializing classes.")
    }
}