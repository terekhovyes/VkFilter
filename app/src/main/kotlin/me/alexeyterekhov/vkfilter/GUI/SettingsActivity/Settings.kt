package me.alexeyterekhov.vkfilter.GUI.SettingsActivity

import android.content.SharedPreferences


public object Settings {
    private val KEY_NOTIFICATIONS = "pref_notifications"
    private val KEY_VIBRATION = "pref_vibration"
    private val KEY_SOUND = "pref_sound"
    private val KEY_COLOR_LIGHT = "pref_color_lights"
    private val KEY_DEMONSTRATE_ATTACHMENTS = "pref_attachments_demo"

    // Notifications
    fun notificationsEnabled(s: SharedPreferences) = s.getBoolean(KEY_NOTIFICATIONS, true)
    fun allowVibration(s: SharedPreferences) = s.getBoolean(KEY_VIBRATION, true)
    fun allowSound(s: SharedPreferences) = s.getBoolean(KEY_SOUND, false)
    fun allowCustomLights(s: SharedPreferences) = s.getBoolean(KEY_COLOR_LIGHT, false)

    // Attachments demonstration
    fun getAttachmentsOpenings(s: SharedPreferences) = s.getInt(KEY_DEMONSTRATE_ATTACHMENTS, 0)
    fun setAttachmentsOpenings(s: SharedPreferences, count: Int) {
        s
            .edit()
            .putInt(KEY_DEMONSTRATE_ATTACHMENTS, count)
            .commit()
    }
}