package com.azaldev.garden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = bytes.joinToString("") { "%02x".format(it) }
//        Log.d("devl|hash", "Hashed string $input to $result")
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val globals = Globals()
        val contextView = findViewById<View>(R.id.loginLayoutCtx)

        Utilities.hasInternetConnection(this) { isConnected ->
            globals.has_connection = isConnected
            if (!isConnected) {
                Snackbar.make(contextView, "You are not connected to the internet, You wont have access to cloud features", Snackbar.LENGTH_LONG)
                    .setAction("Recheck") {

                    }
                    .setActionTextColor(ContextCompat.getColor(this, R.color.red_400))
                    .show()
            }
        }

        findViewById<ExtendedFloatingActionButton>(R.id.login_button).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailField).text.toString()
            val password = findViewById<TextInputEditText>(R.id.passwordField).text.toString()

            Log.d("devl|login", "Email $email and password $password")

            if (email == "" || password == "") {
                Log.d("devl|login", "Invalid email $email or password $password")

                Snackbar.make(contextView, "Invalid username or password", Snackbar.LENGTH_SHORT)
                    .setAction("Try Again") {
                        findViewById<TextInputEditText>(R.id.emailField).requestFocus()
                    }
                    .setActionTextColor(ContextCompat.getColor(this, R.color.red_400))
                    .show()
                return@setOnClickListener
            }

            /**
             * Hash email and password for enhanced security and privacy
             */
            var hEmail: String = sha256(email)
            var hPassword: String = sha256(password)

            Log.d("devl|login", "#1 Hashed email $hEmail and password $hPassword")

            if (hEmail == "null" || hPassword == "null") {
                Snackbar.make(contextView, "Captcha has been successfully verified, you may click the register/login button again", Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(this, R.color.green_400))
                    .show()

                findViewById<ExtendedFloatingActionButton>(R.id.login_button).requestFocus()

                return@setOnClickListener
            }


            val user_object = mapOf("email" to hEmail, "password" to hPassword)

            if (globals.webSocketClient == null && globals.has_connection) {
                globals.webSocketClient = WSClient("https://socko.azaldev.com")
            }

            if (globals.has_connection) {
                globals.webSocketClient?.emit("register", user_object)
                globals.webSocketClient?.on("register") { data ->
//                    Log.d("devl|ws", "Received data: $data")
                    val success = if (data.contains("\"success\"=false")) false else true
                    val message = globals.webSocketClient?.parseMessage(data)

                    if (success) {
                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(this, R.color.green_400))
                            .show()
                    } else {
                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(this, R.color.red_400))
                            .show()
                    }
                }
            }




        }

    }
}