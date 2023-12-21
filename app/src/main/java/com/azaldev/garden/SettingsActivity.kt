package com.azaldev.garden

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.dao.GlobalSettingsDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Globals
import com.azaldev.garden.globals.Utilities
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
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

        val database = AppDatabase.getInstance(applicationContext);
        authDao = database.AuthDao()
        settinsDao = database.GlobalSettingsDao()

        var device_lang = Globals.stored_settings?.lang ?: Locale.getDefault().language

        val toggleButton: MaterialButtonToggleGroup = findViewById(R.id.toggleButton)

        val button_es: Button = findViewById(R.id.button_es)
        val button_en: Button = findViewById(R.id.button_en)
        val button_eu: Button = findViewById(R.id.button_eu)

        val defaultColor = Color.argb(0, 0, 255, 0)
        val colorSelected = ContextCompat.getColor(this, R.color.beige_300)
        val textColorSelected = Color.BLACK

        button_es.backgroundTintList = ColorStateList.valueOf(defaultColor)
        button_en.backgroundTintList = ColorStateList.valueOf(defaultColor)
        button_eu.backgroundTintList = ColorStateList.valueOf(defaultColor)

        /**
         * Set language on load, a bit buggy
         */
        Log.i("devl|settings", "Setting language picker to $device_lang")
        with(device_lang) {
            when (this) {
                "es" -> {
                    toggleButton.check(R.id.button_es);
                    button_es.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_es.setTextColor(textColorSelected)
                }
                "en" -> {
                    toggleButton.check(R.id.button_en);
                    button_en.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_en.setTextColor(textColorSelected)                }
                "eu" -> {
                    toggleButton.check(R.id.button_eu);
                    button_eu.backgroundTintList = ColorStateList.valueOf(colorSelected)
                    button_eu.setTextColor(textColorSelected)                }
            }
        }

        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = findViewById<MaterialButton>(checkedId)
            Log.d("devl|settings", "Checked button ${button.text} to ${isChecked} btn status: ${button.z}")

            if (isChecked) {
                button.backgroundTintList = ColorStateList.valueOf(colorSelected)
                button.setTextColor(textColorSelected)

                val  catche_device_lang = Globals.stored_settings?.lang

                with(button) {
                    when (this) {
                        button_es -> {
                            device_lang = "es"
                        }
                        button_en -> {
                            device_lang = "en"
                        }
                        button_eu -> {
                            device_lang = "eu"
                        }
                    }

                    Utilities.setLocale(this@SettingsActivity, device_lang)
                }

                Log.d("devl|settings", "Language changed from ${catche_device_lang} > ${device_lang}.")

                /**
                 * Update the screen and db settings
                 * We update it just when it's different to avoid spam and repeating loops
                 */
                if (catche_device_lang != device_lang) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        settinsDao.updateLang(device_lang);
                        Globals.stored_settings = settinsDao.get()

                        withContext(Dispatchers.Main) {
                            Log.i("devl|settings", "Recreating settings to reload the language...")
                            recreate() // Restart activity to apply the new locale
                        }
                    }
                }
            } else {
                button.backgroundTintList = ColorStateList.valueOf(defaultColor)
                button.setTextColor(Color.WHITE)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
                // Perform database operations in a background thread
                val auth = authDao.get()
                val email = authDao.email()
                val settings = settinsDao.get()

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

        findViewById<ImageButton>(R.id.games_button).setOnClickListener {
            finish()
        }

        /**
         * Hide the login button if there is a logged user
         * Or Set it to disable if there is a logged user, but it was logged without internet
         */
        val loginButton = findViewById<ImageButton>(R.id.login_button);

        if (Globals.stored_user != null && Globals.stored_user!!.server_synced)
            loginButton.visibility = View.INVISIBLE;
        else if (Globals.stored_user != null && !Globals.stored_user?.server_synced!!)
            loginButton.isClickable = false;

        loginButton.setOnClickListener {
            Utilities.startActivity(this, LoginActivity::class.java)
        }

        val scanQrCode = findViewById<ImageButton>(R.id.camera_button)
        val canUserQrCOde = Globals.stored_user == null || Globals.stored_user?.server_synced!!

        if (!canUserQrCOde) {
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)

            scanQrCode.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        scanQrCode.setOnClickListener {
//            if (!canUserQrCOde) return@setOnClickListener

            Utilities.scanQRCodePop(this, "Join Classroom") {
                val result: String? = it
                Log.i("devl|settings", "Scanned qr code to join classroom, got response $result")

                if (Utilities.isValidCode(result))
                    Utilities.showToast(this, "Joining class: $result")
                else
                    Snackbar.make(scanQrCode, "Invalid QR code format...", Snackbar.LENGTH_LONG)
                        .setAction("Recheck") { scanQrCode.callOnClick() }
                        .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                        .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Perform database operations in a background thread
                val auth = authDao.get()
                val email = authDao.email()
                val settings = settinsDao.get()

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