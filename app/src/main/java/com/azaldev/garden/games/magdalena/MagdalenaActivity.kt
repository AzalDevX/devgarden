package com.azaldev.garden.games.magdalena

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import com.azaldev.garden.R
import com.google.android.material.button.MaterialButton

class MagdalenaActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magdalena)

        videoView = findViewById(R.id.videoView)
        mediaController = MediaController(this)

        // Asigna el MediaController al VideoView
        videoView.setMediaController(mediaController)

        // Establece la ruta del video desde los recursos raw
        val path = "android.resource://" + packageName + "/" + R.raw.alunelul
        videoView.setVideoPath(path)

        // Inicia la reproducción del video
        videoView.start()

        // Mostrar el MediaController y vincularlo al VideoView
        mediaController.show(0)
        mediaController.setAnchorView(videoView)


        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
}