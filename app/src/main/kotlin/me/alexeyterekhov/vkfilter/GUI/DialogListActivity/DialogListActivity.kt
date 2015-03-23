package me.alexeyterekhov.vkfilter.GUI.DialogListActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.vk.sdk.VKSdk
import com.vk.sdk.VKUIHelper
import me.alexeyterekhov.vkfilter.Common.*
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.BrandUI
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList.DialogAdapter
import me.alexeyterekhov.vkfilter.GUI.LoginActivity.LoginActivity
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollListener
import me.alexeyterekhov.vkfilter.LibClasses.RecyclerItemClickAdapter
import me.alexeyterekhov.vkfilter.R

public open class DialogListActivity:
    ActionBarActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    DataDepend
{
    private val TITLE_REFRESH_PERIOD = 5000L
    private val DIALOG_LOAD_PORTION = 50
    private val DIALOG_OFFSET_START_LOADING = 15
    private val SHOW_REFRESH_ICON_OFFSET = 800L

    private val handler = Handler()
    private var animateDialogRefreshing = true

    private val glassModule = ActivityGlassModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        VKUIHelper.onCreate(this)
        onCreateOrRestart()
    }
    override fun onRestart() {
        super<ActionBarActivity>.onRestart()
        onCreateOrRestart()
    }
    private fun onCreateOrRestart() {
        setContentView(R.layout.activity_dialog)
        if (android.os.Build.VERSION.SDK_INT <= 20)
            BrandUI.brandScrollEffectColors()

        glassModule.onCreate()

        with (findDialogList()) {
            if (getAdapter() == null) setAdapter(DialogAdapter(this))
            if (getLayoutManager() == null) setLayoutManager(LinearLayoutManager(AppContext.instance))
            if (getItemAnimator() == null) setItemAnimator(DefaultItemAnimator())
            getItemAnimator() setSupportsChangeAnimations true
            addOnItemTouchListener(RecyclerItemClickAdapter(
                    AppContext.instance,
                    object : RecyclerItemClickAdapter.OnItemClickListener {
                        override fun onItemClick(view: View, pos: Int) {
                            val dialog = getAdapter() as DialogAdapter getDialog pos
                            val dialogId = dialog.id.toString()
                            val intent = Intent(this@DialogListActivity, javaClass<ChatActivity>())
                            val key = if (dialog.isChat()) "chat_id" else "user_id"
                            intent.putExtra(key, dialogId)
                            intent.putExtra("title", dialog.title)
                            if (DataSaver contains ChatActivity.KEY_SAVED) {
                                val chatAdapter = (DataSaver removeObject ChatActivity.KEY_ADAPTER)
                                        as MessageListAdapter?
                                if (chatAdapter != null) {
                                    chatAdapter.firstLoad = true
                                    DataSaver.putObject(ChatActivity.KEY_ADAPTER, chatAdapter)
                                }
                            }
                            startActivity(intent)
                            overridePendingTransition(R.anim.activity_from_right, R.anim.activity_to_left)
                        }
                    }))
            setOnScrollListener(object : EndlessScrollListener(
                    recyclerView = this,
                    activationThreshold = DIALOG_OFFSET_START_LOADING
            ) {
                override fun onReachThreshold(currentItemCount: Int) {
                    loadDialogs(currentItemCount)
                }
            })
        }

        // Initialization of refresh layout
        with (findRefreshLayout()) {
            setOnRefreshListener(this@DialogListActivity)
            setRecyclerView(findDialogList())
            setColorSchemeResources(
                    R.color.refresh_color_1,
                    R.color.refresh_color_2,
                    R.color.refresh_color_3,
                    R.color.refresh_color_4
            )
        }

        // Subscribe on cache
        if (!DialogListCache.listeners.contains(this))
            DialogListCache.listeners.add(this)
        onDataUpdate()
    }
    override fun onResume() {
        super<ActionBarActivity>.onResume()
        VkSdkInitializer.init()
        if (!VKSdk.wakeUpSession(this))
            startActivity(Intent(this, javaClass<LoginActivity>()))
        VKUIHelper.onResume(this)

        refreshActionBar()
        VkRequestControl.resume()
        onRefresh()
        subscribeOnGCM()
        startRefreshingActionBar()
    }
    override fun onPause() {
        super<ActionBarActivity>.onPause()
        unsubscribeFromGCM()
        VkRequestControl.pause()
        stopRefreshingActionBar()
    }
    override fun onDestroy() {
        super<ActionBarActivity>.onDestroy()
        VKUIHelper.onDestroy(this)
        glassModule.onDestroy()
        DialogListCache.listeners remove this
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<ActionBarActivity>.onSaveInstanceState(outState)
        glassModule.saveState()
    }

    override fun onBackPressed() {
        if (glassModule.isShown())
            glassModule.hide()
        else
            super<ActionBarActivity>.onBackPressed()
    }

    fun getDialogAdapter() = findDialogList().getAdapter() as DialogAdapter?
    private fun findDialogList() = findViewById(R.id.dialogList) as RecyclerView
    private fun findRefreshLayout() = findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout

    private val refreshActionBarAction = object : Runnable {
        override fun run() {
            refreshActionBar()
            handler.postDelayed(this, TITLE_REFRESH_PERIOD)
        }
    }
    private fun refreshActionBar() {
        val lastUpdateTime = DialogListCache.getSnapshot().snapshotTime
        getSupportActionBar().setSubtitle(
                if (lastUpdateTime == 0L)
                    getString(R.string.there_are_no_update)
                else
                    getString(R.string.last_update) + " " + DateFormat.lastUpdateTime(lastUpdateTime)
        )
    }
    private fun startRefreshingActionBar() {
        handler.postDelayed(refreshActionBarAction, TITLE_REFRESH_PERIOD)
    }
    private fun stopRefreshingActionBar() {
        handler.removeCallbacks(refreshActionBarAction)
    }

    private val GCMListener = object : IntentListener {
        override fun onGetIntent(intent: Intent) {
            val savedValue = animateDialogRefreshing
            animateDialogRefreshing = false
            onRefresh()
            animateDialogRefreshing = savedValue
        }
    }
    private fun subscribeOnGCM() { ReceiverStation.listener = GCMListener }
    private fun unsubscribeFromGCM() {
        if (ReceiverStation.listener == GCMListener)
            ReceiverStation.listener = null
    }

    private fun loadDialogs(offset: Int) = RunFun.dialogList(offset, DIALOG_LOAD_PORTION)

    private val showRefreshingIcon = Runnable {
         findRefreshLayout() setRefreshing animateDialogRefreshing
    }
    override fun onRefresh() {
        handler.postDelayed(showRefreshingIcon, SHOW_REFRESH_ICON_OFFSET)
        loadDialogs(0)
    }

    override fun onDataUpdate() {
        val adapter = getDialogAdapter()!!
        adapter.checkDialogCache()
        handler removeCallbacks showRefreshingIcon
        findRefreshLayout() setRefreshing false
        stopRefreshingActionBar()
        refreshActionBar()
        startRefreshingActionBar()
    }
}