package com.azaldev.garden.com

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


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

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = bytes.joinToString("") { "%02x".format(it) }
        return result
    }

    fun parseMessage(jsonString: String): String? {
        val cleanedJsonString = jsonString.trim()
        if (cleanedJsonString.startsWith("{") && cleanedJsonString.endsWith("}")) {
            val keyValuePairs = cleanedJsonString.substring(1, cleanedJsonString.length - 1)
                .split(",")
                .map { it.trim() }
            val messagePair = keyValuePairs.find { it.startsWith("\"message\":") }
            return messagePair?.substringAfter("\":\"")?.substringBefore("\"")
        } else {
            throw IllegalArgumentException("Invalid JSON format: $jsonString")
        }
    }

    private fun mapToJson(data: Map<String, Any>): String {
        // Convert the Map to a JSON string (you can use a JSON library for better handling)
        return "{${data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }}}"
    }

    private fun jsonToMap(jsonString: String): Map<String, Any> {
        val cleanedJsonString = jsonString.trim()
        if (cleanedJsonString.startsWith("{") && cleanedJsonString.endsWith("}")) {
            val keyValuePairs = cleanedJsonString.substring(1, cleanedJsonString.length - 1)
                .split(",")
                .map { it.trim() }
            return keyValuePairs.associate { pair ->
                val (key, value) = pair.split(":").map { it.trim() }
                key to toJavaType(value)
            }
        } else {
            throw IllegalArgumentException("Invalid JSON format: $jsonString")
        }
    }

    private fun toJavaType(value: String): Any {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> value.substring(1, value.length - 1)
            value.toBooleanStrictOrNull() != null -> value.toBoolean()
            value.toDoubleOrNull() != null -> value.toDouble()
            value.toFloatOrNull() != null -> value.toFloat()
            value.toLongOrNull() != null -> value.toLong()
            value.toIntOrNull() != null -> value.toInt()
            else -> throw IllegalArgumentException("Unsupported JSON value: $value")
        }
    }
}
