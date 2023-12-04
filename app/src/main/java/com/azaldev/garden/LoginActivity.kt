package com.azaldev.garden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.azaldev.garden.com.WSClient
import com.azaldev.garden.globals.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val globals = Globals()
        val contextView = findViewById<View>(R.id.loginLayoutCtx)

        Utilities.hasInternetConnection(this) { isConnected ->
            globals.has_connection = isConnected
            if (!isConnected) {

                Snackbar.make(contextView, "asdsadasdas", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        findViewById<ExtendedFloatingActionButton>(R.id.login_button).setOnClickListener {
            var username = findViewById<TextInputEditText>(R.id.emailField).text.toString()
            var password = findViewById<TextInputEditText>(R.id.passwordField).text.toString()
            username = globals.webSocketClient?.sha256(username).toString()
            password = globals.webSocketClient?.sha256(password).toString()
            Log.d("devl|login", "Username: $username")
            Log.d("devl|login", "Password: $password")
            val user_object = mapOf("email" to username, "password" to password)

            if (username == "" || password == "") {
                Snackbar.make(contextView, "err", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (globals.webSocketClient == null && globals.has_connection) {
                globals.webSocketClient = WSClient("https://socko.azaldev.com")
            }

            if (globals.has_connection) {
                globals.webSocketClient?.emit("register", user_object)
                globals.webSocketClient?.on("register") { data ->
                    Log.d("devl|ws", "Received data: $data")
                    val success = if (data.contains("\"success\"=false")) false else true
                    val message = globals.webSocketClient?.parseMessage(data)

                    if (success) {
                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                    } else {
                        Snackbar.make(contextView, message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }




        }

    }
}