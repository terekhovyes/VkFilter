//package me.alexeyterekhov.vkfilter.GUI.DialogsActivity
//
//import android.content.Intent
//import android.os.Handler
//import android.support.v4.widget.SwipeRefreshLayout
//import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
//import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
//import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
//import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.IntentHandler
//import me.alexeyterekhov.vkfilter.NotificationService.IntentListener
//import me.alexeyterekhov.vkfilter.R
//
//class RefreshModule(val activity: DialogsActivity) {
//    companion object {
//        val INDICATOR_SHOW_OFFSET = 800L
//    }
//
//    val handler = Handler()
//    val showIndicatorAction = createShowIndicatorAction()
//    val refreshListener = createRefreshListener()
//    val cacheListener = createCacheListener()
//    val GCMListener = createGCMListener()
//
//    fun onCreate() {
//        val refreshLayout = findRefreshLayout()
//        with (refreshLayout) {
//            setOnRefreshListener(refreshListener)
//            setRecyclerView(activity.dialogListModule.findList())
//            setColorSchemeResources(
//                    R.color.ui_refresh1,
//                    R.color.ui_refresh2,
//                    R.color.ui_refresh3,
//                    R.color.ui_refresh4
//            )
//        }
//        DialogListCache.listeners.add(cacheListener)
//    }
//
//    fun onResume() {
//        refreshDialogs(withIndicator = true)
//        IntentHandler.addIntentListener(GCMListener)
//        IntentHandler.allowLoadingNotifications(false)
//    }
//
//    fun onPause() {
//        IntentHandler.allowLoadingNotifications(true)
//        IntentHandler.removeIntentListener(GCMListener)
//    }
//
//    fun onDestroy() {
//        DialogListCache.listeners.remove(cacheListener)
//    }
//
//    fun findRefreshLayout() = activity.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout
//
//    fun refreshDialogs(withIndicator: Boolean) {
//        if (withIndicator)
//            handler.postDelayed(showIndicatorAction, INDICATOR_SHOW_OFFSET)
//        activity.requestModule.loadDialogs(0, DialogListModule.LOADING_PORTION)
//    }
//
//    private fun createRefreshListener() = SwipeRefreshLayout.OnRefreshListener { refreshDialogs(withIndicator = true) }
//    private fun createShowIndicatorAction() = Runnable {
//        findRefreshLayout().isRefreshing = true
//    }
//    private fun createCacheListener() = object : DataDepend {
//        override fun onDataUpdate() {
//            handler.removeCallbacks(showIndicatorAction)
//            findRefreshLayout().isRefreshing = false
//        }
//    }
//    private fun createGCMListener() = object : IntentListener {
//        override fun onGetIntent(intent: Intent) {
//            refreshDialogs(withIndicator = false)
//        }
//    }
//}