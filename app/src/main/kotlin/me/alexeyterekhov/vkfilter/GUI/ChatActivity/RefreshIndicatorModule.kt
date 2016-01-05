package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.os.Handler
import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
import me.alexeyterekhov.vkfilter.R

class RefreshIndicatorModule(val chatActivity: ChatActivity) {
    private val MINIMUM_SHOWING_MILLIS = 500L
    private val SHOW_DELAY_MILLIS = 1000L

    private val handler = Handler()
    private var isShowing = false
    private var showTime = 0L
    private var showRunnable = createRefreshRunnable(true)
    private var hideRunnable = createRefreshRunnable(false)

    fun onCreate() {
        with (findLayout()) {
            setDenySwiping(true)
            setColorSchemeResources(
                    R.color.ui_refresh1,
                    R.color.ui_refresh2,
                    R.color.ui_refresh3,
                    R.color.ui_refresh4)
        }
    }

    fun showImmediately() {
        handler.removeCallbacks(hideRunnable)

        if (!isShowing)
            showRunnable.run()
    }

    fun showDelayed() {
        handler.removeCallbacks(hideRunnable)

        if (!isShowing)
            handler.postDelayed(showRunnable, SHOW_DELAY_MILLIS)
    }

    fun hide() {
        handler.removeCallbacks(showRunnable)

        if (isShowing) {
            if (showTime - System.currentTimeMillis() > MINIMUM_SHOWING_MILLIS)
                hideRunnable.run()
            else
                handler.postDelayed(hideRunnable, MINIMUM_SHOWING_MILLIS + showTime - System.currentTimeMillis())
        }
    }

    private fun createRefreshRunnable(refreshingState: Boolean): Runnable {
        return Runnable {
            findLayout().isRefreshing = refreshingState
            isShowing = refreshingState
            if (isShowing)
                showTime = System.currentTimeMillis()
        }
    }

    private fun findLayout() = chatActivity.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout
}