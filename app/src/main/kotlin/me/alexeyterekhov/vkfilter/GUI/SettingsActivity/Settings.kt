package me.alexeyterekhov.vkfilter.GUI.SettingsActivity

import android.content.SharedPreferences

/**
 * Created by Alexey on 09.04.2015.
 */
public object Settings {
    private val KEY_NOTIFICATIONS = "pref_notifications"
    private val KEY_VIBRATION = "pref_vibration"
    private val KEY_SOUND = "pref_sound"
    private val KEY_COLOR_LIGHT = "pref_color_lights"

    fun notificationsEnabled(s: SharedPreferences) = s.getBoolean(KEY_NOTIFICATIONS, false)
    fun allowVibration(s: SharedPreferences) = s.getBoolean(KEY_VIBRATION, true)
    fun allowSound(s: SharedPreferences) = s.getBoolean(KEY_SOUND, true)
    fun allowCustomLights(s: SharedPreferences) = s.getBoolean(KEY_COLOR_LIGHT, true)
}