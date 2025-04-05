package com.xforia.voicecontrol

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager



class TransientActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }
//        val gifImageView: ImageView = findViewById(R.id.iv_gif)
//        Glide.with(this)
//            .asGif()
//            .load(R.drawable.mrbean)
//            .into(gifImageView)
        val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener { focusChange ->

                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        Log.d("AppMonitor", "Audio focus lost, pause media playback")
                    }
                }
                .build()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("AppMonitor", "Audio focus granted")
        } else {
            Log.d("AppMonitor", "Audio focus request failed")
        }

        Log.d("TransientActivity", "Launching temporary foreground activity")
        val filter = IntentFilter("com.ionic.OVERLAY_SERVICE_STOPPED")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(serviceStopReceiver, filter)
    }
    private val serviceStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("TransientActivity", "Received stop signal, closing activity")
            finish() // Close the activity
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStopReceiver) // Clean up
    }
}



