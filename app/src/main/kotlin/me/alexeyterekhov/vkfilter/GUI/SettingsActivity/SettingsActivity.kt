package me.alexeyterekhov.vkfilter.GUI.SettingsActivity

import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.widget.LinearLayout
import me.alexeyterekhov.vkfilter.R

class SettingsActivity: PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_settings_theme)
        addPreferencesFromResource(R.xml.app_settings)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val root = findViewById(android.R.id.list).getParent().getParent().getParent() as LinearLayout
        val toolbar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false) as Toolbar
        root.addView(toolbar, 0)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}