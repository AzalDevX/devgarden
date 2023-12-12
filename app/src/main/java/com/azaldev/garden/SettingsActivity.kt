package com.azaldev.garden

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.dao.GlobalSettingsDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class SettingsActivity : AppCompatActivity() {
    private lateinit var authDao: AuthDao;
    private lateinit var settinsDao: GlobalSettingsDao;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val device_lang = Locale.getDefault().language
        val toggleButton: MaterialButtonToggleGroup = findViewById(R.id.toggleButton)

        val button_es: Button = findViewById(R.id.button_es)
        val button_en: Button = findViewById(R.id.button_en)
        val button_eu: Button = findViewById(R.id.button_eu)

        val defaultColor = Color.argb(0, 0, 255, 0)
        val colorSelected = ContextCompat.getColor(this, R.color.blue_200)
        val textColorSelected = Color.BLACK

        button_es.backgroundTintList = ColorStateList.valueOf(defaultColor)
        button_en.backgroundTintList = ColorStateList.valueOf(defaultColor)
        button_eu.backgroundTintList = ColorStateList.valueOf(defaultColor)

        with(device_lang) {
            when (this) {
                "es" -> {
                    button_es.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_es.setTextColor(textColorSelected)
                    button_es.isSelected = true
                    button_es.isActivated = true
                }
                "en" -> {
                    button_en.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_en.setTextColor(textColorSelected)
                    button_en.isSelected = true
                    button_en.isActivated = true
                }
                "eu" -> {
                    button_eu.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_eu.setTextColor(textColorSelected)
                    button_eu.isSelected = true
                    button_eu.isActivated = true
                }
            }
        }

        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = findViewById<MaterialButton>(checkedId)
            Log.d("devl|settings", "Checked button: ${button.text}")


            if (isChecked) {
                button.backgroundTintList = ColorStateList.valueOf(colorSelected)
                button.setTextColor(textColorSelected)

                with(button) {
                    when (this) {
                        button_es -> {
                            Utilities.setLocale(this@SettingsActivity, "es")
                        }
                        button_en -> {
                            Utilities.setLocale(this@SettingsActivity, "en")
                        }
                        button_eu -> {
                            Utilities.setLocale(this@SettingsActivity, "eu")
                        }
                    }
                }

                Log.d("devl|settings", "Language changed to ${Locale.getDefault().language}.")

                if (Locale.getDefault().language != device_lang)
                    recreate() // Restart activity to apply the new locale
            } else {
                button.backgroundTintList = ColorStateList.valueOf(defaultColor)
                button.setTextColor(Color.WHITE)
            }
        }

        val database = AppDatabase.getInstance(applicationContext);
        authDao = database.AuthDao()
        settinsDao = database.GlobalSettingsDao()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Perform database operations in a background thread
                val auth = authDao.get()
                val email = authDao.email()
                val settings = settinsDao.getGlobalSettings()

                Log.d("devl|settings", "Getting user auth data...")
                Log.d("devl|settings", "Email: $email")
                Log.d("devl|settings", settings.toString())

                withContext(Dispatchers.Main) {
                    // Update UI on the main thread
                    if (auth != null) {
                        findViewById<TextView>(R.id.userUserTv).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.userEmailTv).text = auth.email

                        if (auth.server_synced)
                            findViewById<TextView>(R.id.userCodeTv).text = auth.code
                        else
                            findViewById<TextView>(R.id.userCodeTv).text = resources.getString(R.string.offline_account)

                        findViewById<TextView>(R.id.userCodeTv)
                            .setTextColor(
                                if (auth.server_synced)
                                    ContextCompat.getColor(this@SettingsActivity, R.color.green_500)
                                else
                                    ContextCompat.getColor(this@SettingsActivity, R.color.red_200)
                            )

                        findViewById<TextView>(R.id.qrTextView).visibility = if(auth.server_synced) View.INVISIBLE else View.VISIBLE

                        Log.d("devl|settings", "Logged user email ${auth.email}")
                    } else
                        Log.d("devl|settings", "No logged user data found.")
                }
            }
        }

        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        findViewById<FloatingActionButton>(R.id.login_button).setOnClickListener {
            Utilities.startActivity(this, LoginActivity::class.java)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Perform database operations in a background thread
                val auth = authDao.get()
                val email = authDao.email()
                val settings = settinsDao.getGlobalSettings()

                Log.d("devl|settings", "Getting user auth data...")
                Log.d("devl|settings", "Email: $email")
                Log.d("devl|settings", settings.toString())

                withContext(Dispatchers.Main) {
                    // Update UI on the main thread
                    if (auth != null) {
                        findViewById<TextView>(R.id.userUserTv).visibility = View.VISIBLE
                        findViewById<TextView>(R.id.userEmailTv).text = auth.email

                        if (auth.server_synced)
                            findViewById<TextView>(R.id.userCodeTv).text = auth.code
                        else
                            findViewById<TextView>(R.id.userCodeTv).text = resources.getString(R.string.offline_account)

                        findViewById<TextView>(R.id.userCodeTv)
                            .setTextColor(
                                if (auth.server_synced)
                                    ContextCompat.getColor(this@SettingsActivity, R.color.green_500)
                                else
                                    ContextCompat.getColor(this@SettingsActivity, R.color.red_200)
                            )

                        findViewById<TextView>(R.id.qrTextView).visibility = if(auth.server_synced) View.INVISIBLE else View.VISIBLE

                        Log.d("devl|settings", "Logged user email ${auth.email}")
                    } else
                        Log.d("devl|settings", "No logged user data found.")
                }
            }
        }
    }
}