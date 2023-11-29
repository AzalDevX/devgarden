package com.azaldev.garden.globals

import android.app.Application

class Globals : Application(){
    var enviroment: String = "development"; // development or production
    var app_language = "eu";

    val languages = arrayOf("Español", "English", "Euskera")
    var user_name : String = "Unknown Name"
}