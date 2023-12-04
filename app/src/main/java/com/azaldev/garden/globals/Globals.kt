package com.azaldev.garden.globals

import android.app.Application
import com.azaldev.garden.com.WSClient

class Globals : Application(){
    var enviroment: String = "development"; // development or production
    var app_language = "eu";
    var has_connection = false;

    val languages = arrayOf("Español", "English", "Euskera")
    var user_name : String = "Unknown Name"

    var webSocketClient : WSClient? = null
}