package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.os.Handler
import android.support.v7.widget.Toolbar
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.DateFormat

class ToolbarModule(val activity: DialogsActivity) {
    companion object {
        val SUBTITLE_REFRESH_PERIOD = 5000L
    }

    private val handler = Handler()
    private val updateSubtitleAction = createUpdateSubtitleAction()
    private val cacheListener = createCacheListener()

    fun onCreate() {
        val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
        activity.setSupportActionBar(toolbar)
        with (activity.getSupportActionBar()) {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    fun onResume() {
        updateSubtitle()
        startRefreshingSubtitle()
        DialogListCache.listeners add cacheListener
    }

    fun onPause() {
        DialogListCache.listeners remove cacheListener
        stopRefreshingSubtitle()
    }

    private fun updateSubtitle() {
        val lastUpdateTime = DialogListCache.getSnapshot().snapshotTime
        activity.getSupportActionBar().setSubtitle(
                if (lastUpdateTime == 0L)
                    activity.getString(R.string.dialog_label_toolbar_no_update)
                else
                    activity.getString(R.string.dialog_label_toolbar_update) + " " + DateFormat.lastUpdateTime(lastUpdateTime)
        )
    }

    private fun startRefreshingSubtitle() {
        handler.postDelayed(updateSubtitleAction, SUBTITLE_REFRESH_PERIOD)
    }

    private fun stopRefreshingSubtitle() {
        handler.removeCallbacks(updateSubtitleAction)
    }

    private fun createUpdateSubtitleAction() = object : Runnable {
        override fun run() {
            updateSubtitle()
            handler.postDelayed(this, SUBTITLE_REFRESH_PERIOD)
        }
    }
    private fun createCacheListener() = object : DataDepend {
        override fun onDataUpdate() {
            stopRefreshingSubtitle()
            updateSubtitle()
            startRefreshingSubtitle()
        }
    }
}