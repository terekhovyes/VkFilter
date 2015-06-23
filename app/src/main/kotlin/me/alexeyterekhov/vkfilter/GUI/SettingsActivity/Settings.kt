package me.alexeyterekhov.vkfilter.GUI.SettingsActivity

import android.content.SharedPreferences
import android.preference.PreferenceManager
import me.alexeyterekhov.vkfilter.Util.AppContext


public object Settings {
    private val KEY_NOTIFICATIONS = "pref_notifications"
    private val KEY_VIBRATION = "pref_vibration"
    private val KEY_SOUND = "pref_sound"
    private val KEY_COLOR_LIGHT = "pref_color_lights"
    private val KEY_DEMONSTRATE_ATTACHMENTS = "pref_attachments_demo"

    private fun defaultPreferences() = PreferenceManager.getDefaultSharedPreferences(AppContext.instance)

    // Notifications
    fun notificationsEnabled(s: SharedPreferences) = s.getBoolean(KEY_NOTIFICATIONS, true)
    fun allowVibration(s: SharedPreferences) = s.getBoolean(KEY_VIBRATION, true)
    fun allowSound(s: SharedPreferences) = s.getBoolean(KEY_SOUND, false)
    fun allowCustomLights(s: SharedPreferences) = s.getBoolean(KEY_COLOR_LIGHT, false)

    // Attachments demonstration
    fun getAttachmentsOpenings(s: SharedPreferences = defaultPreferences()) = s.getInt(KEY_DEMONSTRATE_ATTACHMENTS, 0)
    fun setAttachmentsOpenings(count: Int, s: SharedPreferences = defaultPreferences()) {
        s
            .edit()
            .putInt(KEY_DEMONSTRATE_ATTACHMENTS, count)
            .commit()
    }
}