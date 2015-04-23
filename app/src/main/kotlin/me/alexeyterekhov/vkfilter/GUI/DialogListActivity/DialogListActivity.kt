package me.alexeyterekhov.vkfilter.GUI.DialogListActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.DataSaver
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.BrandUI
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList.MessageListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList.DialogAdapter
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.SettingsActivity
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollListener
import me.alexeyterekhov.vkfilter.LibClasses.RecyclerItemClickAdapter
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import me.alexeyterekhov.vkfilter.NotificationService.IntentListener
import me.alexeyterekhov.vkfilter.NotificationService.NotificationMaker
import me.alexeyterekhov.vkfilter.R

public open class DialogListActivity:
    VkActivity(),
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
        super<VkActivity>.onCreate(savedInstanceState)
        onCreateOrRestart()
    }
    override fun onRestart() {
        super<VkActivity>.onRestart()
        onCreateOrRestart()
    }
    private fun onCreateOrRestart() {
        setContentView(R.layout.activity_dialog)
        if (android.os.Build.VERSION.SDK_INT <= 20)
            BrandUI.brandScrollEffectColors()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawerLayout = findViewById(R.id.side_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.a_dialog_title,
                R.string.a_dialog_title
        ) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                showMeInSideMenu()
            }
        }
        drawerLayout setDrawerListener toggle
        with (getSupportActionBar()) {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        toggle.syncState()

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
        val refreshLayout = findRefreshLayout()
        with (refreshLayout) {
            setOnRefreshListener(this@DialogListActivity)
            setRecyclerView(findDialogList())
            setColorSchemeResources(
                    R.color.my_refresh_1,
                    R.color.my_refresh_2,
                    R.color.my_refresh_3,
                    R.color.my_refresh_4
            )
        }

        findViewById(R.id.logout_button) as Button setOnClickListener {
            GCMStation.onLogout()
            VKSdk.logout()
            super<VkActivity>.toLoginActivity()
        }

        findViewById(R.id.settings_button) as Button setOnClickListener {
            startActivity(Intent(this, javaClass<SettingsActivity>()))
        }

        // Subscribe on cache
        if (!DialogListCache.listeners.contains(this))
            DialogListCache.listeners.add(this)
        onDataUpdate()
    }
    override fun onResume() {
        super<VkActivity>.onResume()
        refreshActionBar()
        VkRequestControl.resume()
        onRefresh()
        subscribeOnGCM()
        startRefreshingActionBar()
        showMeInSideMenu()
        NotificationMaker.clearAllNotifications(AppContext.instance)
    }
    override fun onPause() {
        super<VkActivity>.onPause()
        unsubscribeFromGCM()
        VkRequestControl.pause()
        stopRefreshingActionBar()
    }
    override fun onDestroy() {
        super<VkActivity>.onDestroy()
        glassModule.onDestroy()
        DialogListCache.listeners remove this
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<VkActivity>.onSaveInstanceState(outState)
        glassModule.saveState()
    }

    override fun onBackPressed() {
        if (glassModule.isShown())
            glassModule.hide()
        else
            super<VkActivity>.onBackPressed()
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
                    getString(R.string.a_dialog_no_update)
                else
                    getString(R.string.a_dialog_last_update) + " " + DateFormat.lastUpdateTime(lastUpdateTime)
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
    private fun subscribeOnGCM() {
        GCMStation addRawIntentListener GCMListener
    }
    private fun unsubscribeFromGCM() {
        GCMStation removeRawIntentListener GCMListener
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
        showMeInSideMenu()
        handler removeCallbacks showRefreshingIcon
        findRefreshLayout() setRefreshing false
        stopRefreshingActionBar()
        refreshActionBar()
        startRefreshingActionBar()
    }

    private fun showMeInSideMenu() {
        val photo = findViewById(R.id.my_photo) as ImageView
        val name = findViewById(R.id.my_name) as TextView
        val lastSeen = findViewById(R.id.my_last_seen) as TextView
        val lastSeenTime = findViewById(R.id.my_last_seen_time) as TextView
        if (UserCache.getMe() != null) {
            val me = UserCache.getMe()!!
            ImageLoader.getInstance().displayImage(
                    me.photoUrl,
                    photo
            )
            name setText TextFormat.userTitle(me, false)
            when {
                me.lastOnlineTime == 0L -> {
                    lastSeen setText ""
                    lastSeenTime setText ""
                }
                me.isOnline -> {
                    lastSeen setText TextFormat.userOnlineStatus(me)
                    lastSeenTime setText ""
                }
                else -> {
                    lastSeen setText TextFormat.lastVisitPhrase(me)
                    lastSeenTime setText TextFormat.lastVisitTime(me)
                }
            }
        } else {
            photo setImageResource R.drawable.user_photo_loading
            name setText ""
            lastSeen setText ""
            lastSeenTime setText ""
        }
    }
}