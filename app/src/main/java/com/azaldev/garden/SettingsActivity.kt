package com.azaldev.garden

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.dao.GlobalSettingsDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.Globals
import com.azaldev.garden.globals.PermissionUtils
import com.azaldev.garden.globals.RNGName
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
    private var cacheStoredUser: Auth? = null;
    private lateinit var qr_image : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.bird_left).visibility = View.INVISIBLE
        qr_image = findViewById(R.id.qr_code)
        qr_image.visibility = View.INVISIBLE

        val database = AppDatabase.getInstance(applicationContext);
        authDao = database.AuthDao()
        settinsDao = database.GlobalSettingsDao()

        cacheStoredUser = Globals.stored_user;

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

        val contextView = findViewById<View>(R.id.settingsCtx)

        Utilities.canConnectToApi {
            Globals.has_connection = it

            if (Globals.has_connection && Globals.webSocketClient == null)
                Globals.webSocketClient = WSClient(Globals.api_url)

            Log.i("devl|main", "Internet connection status: $it, WSClient status: ${Globals.webSocketClient != null}")
        }

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
        val loginButton = findViewById<ImageButton>(R.id.login_button)

        if (Globals.stored_user != null && Globals.stored_user!!.server_synced) {
            loginButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.iconsdashboard))

            var camera_button = findViewById<ImageButton>(R.id.camera_button)
            camera_button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_qr_code_2_24))

            val qrData = Globals.stored_user?.code   // Teacher code
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrData.toString(), BarcodeFormat.QR_CODE, 300, 300)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            val paddingInDp = 125
            val paddingInPixels = (paddingInDp * resources.displayMetrics.density).toInt()

            qr_image.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels)
            qr_image.setImageBitmap(bitmap)

            findViewById<TextView>(R.id.scan_text).text = getString(R.string.scan_teacher)


        }
        else if (Globals.stored_user != null && !Globals.stored_user!!.server_synced)
            loginButton.isClickable = false
        else if (Globals.stored_settings?.student_classcode != null) {
            loginButton.visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.icon_settings).visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.bird_left).visibility = View.VISIBLE
        }

        loginButton.setOnClickListener {
            Utilities.startActivity(
                this,
                if (Globals.stored_user == null) LoginActivity::class.java else DashboardActivity::class.java
            )
        }

        val scanQrCode = findViewById<ImageButton>(R.id.camera_button)
        val canUserQrCOde = Globals.stored_user == null || Globals.stored_user?.server_synced!!

        if (!canUserQrCOde) {
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)

            scanQrCode.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        qr_image.setOnClickListener {
            qr_image.visibility = View.INVISIBLE
            //Utilities.setBrightness(this, Utilities.getBrightness(this) - 5       0)
        }

        scanQrCode.setOnClickListener {

            if(Globals.stored_user != null)  {
                qr_image.visibility = View.VISIBLE
                //Utilities.setBrightness(this, Utilities.getBrightness(this) + 50)
                return@setOnClickListener
            }

            if (!canUserQrCOde) return@setOnClickListener
            if (!PermissionUtils.checkAndRequestCameraPermission(this)) return@setOnClickListener

            Utilities.scanQRCodePop(this, "Join Classroom") {
                val result: String? = it
                Log.i("devl|settings", "Scanned qr code to join classroom, got response $result")

                if (!Utilities.isValidCode(result)) {
                    Snackbar.make(scanQrCode, "Invalid QR code format...", Snackbar.LENGTH_LONG)
                        .setAction("Try Again") { scanQrCode.callOnClick() }
                        .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.green_200))
                        .show()

                    return@scanQRCodePop
                }

                if (!Globals.has_connection) {
                    Snackbar.make(contextView, "You are not connected to the internet :c", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again") {}
                        .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.green_200))
                        .show()

                    return@scanQRCodePop
                }

                val group_name = RNGName.generate();
                Log.i("devl|settings", "Joining class: $result with the name $group_name")

                val group_object = mapOf("group" to group_name, "class" to result.toString())

                Globals.webSocketClient?.emit("join_class", group_object)
                Globals.webSocketClient?.on("join_class") { data ->
                    val success = Globals.webSocketClient?.parseCustomBoolean(data, "success") ?: false;
                    val already_in_class = Globals.webSocketClient?.parseCustomBoolean(data, "alreadyClass") ?: false;
                    val group_created = Globals.webSocketClient?.parseCustomBoolean(data, "group") ?: false;

                    val message = Globals.webSocketClient?.parseMessage(data);

                    if (success && group_created) {
                        Log.i("devl|settings", "Join Class Succeed, response: $message")

                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(this, R.color.blue_300))
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.green_200))
                            .show()
                    } else {
                        Log.e("devl|settings", "Join Class Failed, response: $message")

                        Snackbar.make(contextView, if (already_in_class) "This group is already in a class." else message.toString(), Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.green_200))
                            .show()

                        return@on
                    }

                    settinsDao.updateStudent(group_name, result.toString())
                    Globals.stored_settings = settinsDao.getDefault()
                    runOnUiThread {
                        Log.i("devl|settings", "Recreating settings to reload the language...")
                        recreate() // Restart activity to apply the new locale
                    }
                }
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

    override fun onResume() {
        super.onResume()
        if (cacheStoredUser != Globals.stored_user)
            recreate();
        Log.i("devl|landing", "onResume() has been called, user is ${if (cacheStoredUser != Globals.stored_user) "changed" else "cached"}")
    }
}