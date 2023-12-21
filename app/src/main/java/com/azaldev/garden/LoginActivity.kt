package com.azaldev.garden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.classes.dao.AuthDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val contextView = findViewById<View>(R.id.loginLayoutCtx)
        val database = AppDatabase.getInstance(applicationContext)
        val authDao = database.AuthDao();

        Utilities.hasInternetConnection(this) { isConnected ->
            Globals.has_connection = isConnected
            if (!isConnected) {
                Snackbar.make(contextView, "You are not connected to the internet, You wont have access to cloud features", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Recheck") {}
                    .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                    .show()
            }
        }

        val loginButton = findViewById<ExtendedFloatingActionButton>(R.id.login_button)
        loginButton.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailField).text.toString()
            val password = findViewById<TextInputEditText>(R.id.passwordField).text.toString()

            Log.d("devl|login", "Email $email and password $password")

            if (email == "" || password == "") {
                Log.d("devl|login", "Invalid email $email or password $password")

                Snackbar.make(contextView, "Invalid username or password", Snackbar.LENGTH_SHORT)
                    .setAction("Try Again") {
                        findViewById<TextInputEditText>(R.id.emailField).requestFocus()
                    }
                    .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                    .show()
                return@setOnClickListener
            }

            /**
             * Hash email and password for enhanced security and privacy
             */
            var hEmail: String = Utilities.sha256(email)
            var hPassword: String = Utilities.sha256(password)

            Log.d("devl|login", "#1 Hashed email $hEmail and password $hPassword")

            if (hEmail == "null" || hPassword == "null") {
                Snackbar.make(contextView, "Captcha has been successfully verified, you may click the register/login button again", Snackbar.LENGTH_LONG)
                    .setTextColor(ContextCompat.getColor(this, R.color.green_600))
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                    .show()

                findViewById<ExtendedFloatingActionButton>(R.id.login_button).requestFocus()

                return@setOnClickListener
            }

            loginButton.shrink()
            Handler().postDelayed({
                loginButton.hide()
            }, 700);

            val user_object = mapOf("email" to hEmail, "password" to hPassword)

            if (Globals.webSocketClient == null && Globals.has_connection) {
                Globals.webSocketClient = WSClient("https://socko.azaldev.com")
            }

            if (Globals.has_connection) {
                Globals.webSocketClient?.emit("register", user_object)
                Globals.webSocketClient?.on("register") { data ->
                    val success = Globals.webSocketClient?.parseCustomBoolean(data, "success") ?: false;
                    val logged_in = Globals.webSocketClient?.parseCustomBoolean(data, "loggedIn") ?: false;
                    val teacher_code = Globals.webSocketClient?.parseCustom(data, "teacherCode");
                    val message = Globals.webSocketClient?.parseMessage(data);

                    if (success) {
                        Log.i("devl|login", "Register/Login Succeed, response: $message")

                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(this, if (logged_in) R.color.green_600 else R.color.blue_600))
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                            .show()
                    } else {
                        Log.e("devl|login", "Register/Login Failed, response: $message")

                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(this, R.color.red_400))
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.blue_200))
                            .show()
                    }

                    var room_user = Auth(
                        email = email,
                        password = hPassword,
                        code = teacher_code,
                        server_synced = success
                    );

                    authDao.insert(room_user);
                }
            }

            if (!Globals.has_connection) {
                lifecycleScope.launch(Dispatchers.IO) {
                    var room_user = Auth(
                        email = email,
                        password = hPassword,
                        code = "",
                        server_synced = false
                    );

                    authDao.insert(room_user);
                    Globals.stored_user = room_user;

                    withContext(Dispatchers.Main) {
                        Snackbar.make(contextView, "Logged in without internet, some features will be unavailable", Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.blue_500))
                            .setBackgroundTint(ContextCompat.getColor(this@LoginActivity, R.color.blue_200))
                            .show()
                    }
                }
            }

            Handler().postDelayed({
                finish();
            }, 4000);
        }

        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            finish() // Ez back bot
        }
    }
}