package com.azaldev.garden.com

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException


class WSClient(private val serverUrl: String) {
    private lateinit var socket: Socket

    init {
        try {
            Log.d("devl|ws", "Initializing WSClient...")
            var options = IO.Options();
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.timeout = 10000;
            options.query = "token=" + "devgarden.azaldev.com";

            socket = IO.socket(serverUrl, options);
            connect();
        } catch (e: Exception) {
            Log.e("devl|ws", e.toString())
        }
    }

    fun connect() {
        Log.d("devl|ws", "Connecting to the server...")
        socket.connect()

        socket.on(Socket.EVENT_CONNECT) {
            Log.i("devl|ws", "Connected to the server")
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("devl|ws", "Disconnected from the server")
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.e("devl|ws", "Connection error: ${it.toString()}")
        }

        socket.on("error") {
            Log.e("devl|ws", "Server error: $it")
        }
    }

    fun disconnect() {
        Log.d("devl|ws", "Disconnecting from the server...")
        socket.disconnect()
    }

    fun emit(event: String, data: String) {
        socket.emit(event, data)
    }

    fun emit(event: String, data: Map<String, Any>) {
        socket.emit(event, mapToJson(data))
    }

    fun on(event: String, callback: (String) -> Unit) {
        socket.on(event) { args ->
            val jsonString = args[0].toString()
            callback.invoke(jsonString)
        }
    }

    private fun mapToJson(data: Map<String, Any>): String {
        // Convert the Map to a JSON string (you can use a JSON library for better handling)
        return "{${data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }}}"
    }
}
