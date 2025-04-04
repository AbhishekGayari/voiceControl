package com.xforia.voicecontrol

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1001
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startBtn = findViewById(R.id.btnStart)
        stopBtn = findViewById(R.id.btnStop)

        startBtn.setOnClickListener {
            Log.e("VoiceService", "Starting Service")
            ContextCompat.startForegroundService(this, Intent(this, VoiceService::class.java))
        }

        stopBtn.setOnClickListener {
            Log.e("VoiceService", "Stopping Service")
            stopService(Intent(this, VoiceService::class.java))
        }

        checkPermissions()
    }
    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.RECORD_AUDIO
        )
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), REQUEST_CODE)

        } else {
            checkMainPermission()
        }
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkMainPermission()
    }
    private fun checkMainPermission(){
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            permissionLauncher.launch(intent)
        }
        else {
            enableButtons()
        }
    }

    private fun enableButtons() {
        startBtn.isEnabled = true
        stopBtn.isEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                checkMainPermission()
            } else {
                finish() // Exit app if permission denied
            }
        }
    }

}