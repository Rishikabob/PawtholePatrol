package com.example.pawtholepatrol

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "pawthole_prefs"
    private const val KEY_AUTO_DETECT = "auto_detect"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_ALERT_DISTANCE = "alert_distance"
    private const val KEY_INQUIRY_VISUAL_ENABLED = "inquiry_visual_enabled"
    private const val KEY_INQUIRY_AUDIO_ENABLED = "inquiry_audio_enabled"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoDetectEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_DETECT, true)

    fun setAutoDetectEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_DETECT, enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isInquiryVisualEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_INQUIRY_VISUAL_ENABLED, true)

    fun setInquiryVisualEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_INQUIRY_VISUAL_ENABLED, enabled).apply()
    }

    fun isInquiryAudioEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_INQUIRY_AUDIO_ENABLED, true)

    fun setInquiryAudioEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_INQUIRY_AUDIO_ENABLED, enabled).apply()
    }

    fun getAlertDistanceMeters(context: Context): Int =
        prefs(context).getInt(KEY_ALERT_DISTANCE, 120)

    fun setAlertDistanceMeters(context: Context, distanceMeters: Int) {
        prefs(context).edit().putInt(KEY_ALERT_DISTANCE, distanceMeters).apply()
    }
}
