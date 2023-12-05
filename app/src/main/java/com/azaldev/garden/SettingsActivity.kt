package com.azaldev.garden

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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


class SettingsActivity : AppCompatActivity() {
    private lateinit var authDao: AuthDao;
    private lateinit var settinsDao: GlobalSettingsDao;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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