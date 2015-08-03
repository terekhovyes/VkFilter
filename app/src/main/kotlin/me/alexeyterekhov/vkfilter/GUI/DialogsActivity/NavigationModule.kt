package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.content.Intent
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.SettingsActivity
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Test.ChatTestActivity
import me.alexeyterekhov.vkfilter.Util.TextFormat

class NavigationModule(val activity: DialogsActivity, val toLoginActivityAction: () -> Unit) {
    val cacheListener = createCacheListener()

    fun onCreate() {
        val drawerLayout = findDrawer()
        val appbarToggle = object : ActionBarDrawerToggle(
                activity,
                drawerLayout,
                activity.findViewById(R.id.toolbar) as Toolbar,
                R.string.dialog_label_toolbar_title,
                R.string.dialog_label_toolbar_title
        ) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                updateMyData()
            }
        }
        drawerLayout setDrawerListener appbarToggle
        appbarToggle.syncState()

        // Menu buttons
        activity.findViewById(R.id.logout_button) as Button setOnClickListener {
            GCMStation.onLogout()
            VKSdk.logout()
            toLoginActivityAction()
        }
        activity.findViewById(R.id.settings_button) as Button setOnClickListener {
            activity.startActivity(Intent(activity, javaClass<SettingsActivity>()))
        }
        activity.findViewById(R.id.testButton) as Button setOnClickListener {
            val intent = Intent(activity, javaClass<ChatTestActivity>())
            intent.putExtra("chat_id", "test")
            intent.putExtra("title", "Тестирование")
            activity.startActivity(intent)
        }
    }

    fun onResume() {
        DialogListCache.listeners add cacheListener
        updateMyData()
    }

    fun onPause() {
        DialogListCache.listeners remove cacheListener
    }

    fun findDrawer() = activity.findViewById(R.id.drawerLayout) as DrawerLayout

    private fun updateMyData() {
        val photo = activity.findViewById(R.id.my_photo) as ImageView
        val name = activity.findViewById(R.id.my_name) as TextView
        val lastSeen = activity.findViewById(R.id.my_last_seen) as TextView
        val lastSeenTime = activity.findViewById(R.id.my_last_seen_time) as TextView

        if (UserCache.getMe() != null) {
            val me = UserCache.getMe()!!
            ImageLoader.getInstance().displayImage(me.photoUrl, photo)
            name setText TextFormat.userTitle(me, compact = false)
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
            photo setImageResource R.drawable.icon_user_stub
            name setText ""
            lastSeen setText ""
            lastSeenTime setText ""
        }
    }

    private fun createCacheListener() = object : DataDepend {
        override fun onDataUpdate() {
            updateMyData()
        }
    }
}