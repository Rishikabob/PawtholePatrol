package com.example.pawtholepatrol.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.example.pawtholepatrol.AppPreferences
import java.util.*

class NotificationHelper(private val context: Context) {

    private val ALERTS_CHANNEL_ID = "alerts_channel_v1"
    private val CRITICAL_CHANNEL_ID = "critical_channel_v1"

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
            tts?.setSpeechRate(1.1f)
            tts?.setPitch(1.2f)
        }
    }

    fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alertsChannel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            "General Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
        }

        val criticalChannel = NotificationChannel(
            CRITICAL_CHANNEL_ID,
            "Critical Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
        }

        manager.createNotificationChannel(alertsChannel)
        manager.createNotificationChannel(criticalChannel)
    }

    fun showCriticalNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CRITICAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(true) // 🔥 prevents re-sound/vibration spam
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)

        if (AppPreferences.isSoundEnabled(context)) {
            speak(message)
        }
    }

    fun showGeneralNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hazard_alert")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}