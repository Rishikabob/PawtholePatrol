package com.example.pawtholepatrol.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.pawtholepatrol.R
import com.example.pawtholepatrol.utility.ConfirmationReceiver

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var countdownTimer: CountDownTimer? = null
    private val timeoutMs = 10000L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val question = intent?.getStringExtra("question") ?: "Confirm"
        val timeout = intent?.getLongExtra("timeout_ms", timeoutMs) ?: timeoutMs

        val notification = NotificationCompat.Builder(this, "overlay_service_channel")
            .setContentTitle("Pawthole Patrol")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {  // API 34+
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        showOverlay(question, timeout)
        return START_NOT_STICKY
    }

    private fun showOverlay(question: String, timeoutMs: Long) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 48
            width = resources.displayMetrics.widthPixels - (2 * 48)  // 48px padding each side
            x = 0
        }

        overlayView.findViewById<TextView>(R.id.tvQuestion).text = question

        val progressBar = overlayView.findViewById<ProgressBar>(R.id.progressBar)
        val btnYes = overlayView.findViewById<Button>(R.id.btnYes)
        val btnNo = overlayView.findViewById<Button>(R.id.btnNo)

        progressBar.max = 100  // Set explicitly

        btnYes.setOnClickListener { onUserResponse(true) }
        btnNo.setOnClickListener { onUserResponse(false) }

        windowManager.addView(overlayView, params)

        countdownTimer = object : CountDownTimer(timeoutMs, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((millisUntilFinished.toFloat() / timeoutMs) * 100).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                onUserResponse(false)
            }
        }.start()
    }

    private fun onUserResponse(answer: Boolean) {
        countdownTimer?.cancel()

        // Broadcast result so ConfirmationReceiver delivers it to EventConfirmationHelper
        val action = if (answer) ConfirmationReceiver.PRESS_YES else ConfirmationReceiver.PRESS_NO
        sendBroadcast(Intent(action))

        dismiss()
    }

    private fun dismiss() {
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        countdownTimer?.cancel()
        super.onDestroy()
    }
}