package com.xforia.voicecontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat


class VoiceService : Service() {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    override fun onCreate() {
        super.onCreate()
        Log.e("VoiceService", "Service Started")
        startForeground(1, createNotification())

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object :RecognitionListener {
            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                Log.d("VoiceService", "Heard: $spokenText")
                startListening() // restart listening
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.e("VoiceService", "onError: $error")
//                startListening() // recover from error
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        startListening()
    }

    private fun startListening() {
        try {
            recognizer.stopListening()
        } catch (e: DeadObjectException) {
            Log.e("VoiceService", "SpeechRecognizer service is dead: ${e.message}")
            // Optionally, reinitialize the SpeechRecognizer or handle cleanup.
        }
//        recognizer.startListening(recognizerIntent)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                recognizer.startListening(recognizerIntent)
            } catch (e: Exception) {
                Log.e("VoiceService", "startListening failed: ${e.message}")
            }
        }, 500)
    }

    private fun createNotification(): Notification {
        val channelId = "voice_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "Voice Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Service")
            .setContentText("Listening for voice input...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        Log.e("VoiceService", "Noti Service Started")
        return notificationBuilder.build()

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        recognizer.destroy()
        super.onDestroy()
    }
}

