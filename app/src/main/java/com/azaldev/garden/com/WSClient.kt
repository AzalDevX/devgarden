package com.azaldev.garden.com

import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.Headers
import okhttp3.OkHttpClient

class WSClient(private val serverUrl: String) {
    private val socket: Socket

    init {
        // Create a custom OkHttpClient to add headers
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val headers = Headers.Builder()
                    .add("devgarden.azaldev.com", "true") // Add your custom header here
                    .build()
                val newRequest = originalRequest.newBuilder().headers(headers).build()
                chain.proceed(newRequest)
            }
            .build()

        // Set up the socket with the custom OkHttpClient
        val options = IO.Options()
        options.callFactory = client
        options.webSocketFactory = client

        socket = IO.socket(serverUrl, options)

        // Add your event listeners here
        socket.on(Socket.EVENT_CONNECT) {
            println("Connected to the server")
        }
    }

    fun connect() {
        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
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