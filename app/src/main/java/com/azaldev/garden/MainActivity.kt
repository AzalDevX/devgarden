package com.azaldev.garden

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
        Log.i("dev|startup", "App has been started, device information:")
        Log.i("dev|model", "Device Model: ${android.os.Build.MODEL}")
        Log.i("dev|language", "Device Language: ${device_lang}")
        Log.i("dev|version", "Android Version: ${android.os.Build.VERSION.RELEASE}")
        Log.i("dev|screen", "Screen Size: ${resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK}")
        Log.i("dev|resolution", "Screen Resolution: ${resources.displayMetrics.widthPixels.toString() + "x" + resources.displayMetrics.heightPixels.toString()}")
+
        Log.d("dev|main", "MainActivity has started!")

        Log.d("dev|main", "Initializing classes...")
//        val notify = Notify.getInstance(this)
    }
}