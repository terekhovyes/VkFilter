package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.NotificationCollector
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


open class DialogsActivity: VkActivity() {
    companion object {
        val KEY_FILTERPANEL_BUNDLE = "Dialogs_FilterPanel"
    }

    val toolbarModule = ToolbarModule(this)
    val dialogListModule = DialogListModule(this)
    val refreshModule = RefreshModule(this)
    val filterPanelModule = FilterPanelModule(this)
    val requestModule = RequestModule(this)
    val navigationModule = NavigationModule(this, { super<VkActivity>.toLoginActivity() })
    val eventsModule = EventsModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
        toolbarModule.onCreate()
        dialogListModule.onCreate()
        refreshModule.onCreate()
        filterPanelModule.onCreate(savedInstanceState?.getBundle(KEY_FILTERPANEL_BUNDLE))
        navigationModule.onCreate()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbarModule.onPostCreate()
    }

    override fun onResume() {
        super.onResume()
        toolbarModule.onResume()
        refreshModule.onResume()
        navigationModule.onResume()
        NotificationCollector.removeAllNotifications(AppContext.instance)
        eventsModule.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_FILTERPANEL_BUNDLE, filterPanelModule.saveState())
    }

    override fun onPause() {
        super.onPause()
        eventsModule.onPause()
        navigationModule.onPause()
        refreshModule.onPause()
        toolbarModule.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogListModule.onDestroy()
        refreshModule.onDestroy()
        filterPanelModule.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toolbarModule.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (toolbarModule.onOptionsItemSelected(item))
            true
        else
            super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (filterPanelModule.isShown())
            filterPanelModule.hide()
        else
            super.onBackPressed()
    }
}