//package me.alexeyterekhov.vkfilter.GUI.DialogsActivity
//
//import android.content.res.Configuration
//import android.os.Handler
//import android.support.v7.app.ActionBarDrawerToggle
//import android.support.v7.widget.Toolbar
//import android.view.MenuItem
//import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
//import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
//import me.alexeyterekhov.vkfilter.R
//import me.alexeyterekhov.vkfilter.Util.DateFormat
//
//class ToolbarModule(val activity: DialogsActivity) {
//    companion object {
//        val SUBTITLE_REFRESH_PERIOD = 5000L
//    }
//
//    private val handler = Handler()
//    private val updateSubtitleAction = createUpdateSubtitleAction()
//    private val cacheListener = createCacheListener()
//    private var drawerToggle: ActionBarDrawerToggle? = null
//
//    fun onCreate() {
//        val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
//        val drawer = activity.navigationModule.findDrawer()
//        activity.setSupportActionBar(toolbar)
//
//        drawerToggle = ActionBarDrawerToggle(
//                activity,
//                drawer,
//                toolbar,
//                R.string.dialog_navigation_description,
//                R.string.dialog_navigation_description)
//
//        drawer.setDrawerListener(drawerToggle)
//        with (activity.supportActionBar) {
//            setDisplayHomeAsUpEnabled(true)
//            setHomeButtonEnabled(true)
//        }
//    }
//
//    fun onResume() {
//        updateSubtitle()
//        startRefreshingSubtitle()
//        DialogListCache.listeners.add(cacheListener)
//    }
//
//    fun onPause() {
//        DialogListCache.listeners.remove(cacheListener)
//        stopRefreshingSubtitle()
//    }
//
//    fun onPostCreate() {
//        drawerToggle?.syncState()
//    }
//
//    fun onConfigurationChanged(conf: Configuration?) {
//        drawerToggle?.onConfigurationChanged(conf)
//    }
//
//    fun onOptionsItemSelected(item: MenuItem?) = drawerToggle?.onOptionsItemSelected(item) ?: false
//
//    private fun updateSubtitle() {
//        val lastUpdateTime = DialogListCache.getSnapshot().snapshotTime
//        activity.supportActionBar.subtitle = if (lastUpdateTime == 0L)
//            activity.getString(R.string.dialog_label_toolbar_no_update)
//        else
//            activity.getString(R.string.dialog_label_toolbar_update) + " " + DateFormat.lastUpdateTime(lastUpdateTime)
//    }
//
//    private fun startRefreshingSubtitle() {
//        handler.postDelayed(updateSubtitleAction, SUBTITLE_REFRESH_PERIOD)
//    }
//
//    private fun stopRefreshingSubtitle() {
//        handler.removeCallbacks(updateSubtitleAction)
//    }
//
//    private fun createUpdateSubtitleAction() = object : Runnable {
//        override fun run() {
//            updateSubtitle()
//            handler.postDelayed(this, SUBTITLE_REFRESH_PERIOD)
//        }
//    }
//    private fun createCacheListener() = object : DataDepend {
//        override fun onDataUpdate() {
//            stopRefreshingSubtitle()
//            updateSubtitle()
//            startRefreshingSubtitle()
//        }
//    }
//}