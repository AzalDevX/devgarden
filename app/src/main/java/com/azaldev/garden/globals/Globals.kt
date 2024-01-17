package com.azaldev.garden.globals

import android.app.Application
import com.azaldev.garden.classes.entity.Auth
import com.azaldev.garden.classes.entity.GlobalSettings
import com.azaldev.garden.com.WSClient

object Globals : Application(){
    var enviroment: String = "development"; // development or production
    var api_url: String = "https://socko.azaldev.com";
    var app_language = "eu";
    var has_connection = false;
    var ws_api_status = false;

    val languages = arrayOf("Español", "English", "Euskera")
    var user_name : String = "Unknown Name"

    var stored_user: Auth? = null;
    var stored_settings: GlobalSettings? = null;

    var webSocketClient : WSClient? = null
}