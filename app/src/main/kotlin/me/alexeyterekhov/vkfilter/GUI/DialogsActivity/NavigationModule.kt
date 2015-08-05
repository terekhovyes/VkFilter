package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.content.Intent
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.EditFilterActivity
import me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity.ManageFiltersActivity
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.SettingsActivity
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.TextFormat

class NavigationModule(val activity: DialogsActivity, val toLoginActivityAction: () -> Unit) {
    val cacheListener = createCacheListener()

    fun onCreate() {
        val drawerLayout = findDrawer()
        val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                updateMyData()
            }
        }
        drawerLayout setDrawerListener drawerListener

        // Menu
        activity.findViewById(R.id.navigationFiltersButton) setOnClickListener {
            if (DAOFilters.loadVkFilters().isNotEmpty())
                activity.startActivity(Intent(activity, javaClass<ManageFiltersActivity>()))
            else
                activity.startActivity(Intent(activity, javaClass<EditFilterActivity>()))
        }
        activity.findViewById(R.id.navigationPreferencesButton) setOnClickListener {
            activity.startActivity(Intent(activity, javaClass<SettingsActivity>()))
        }
        activity.findViewById(R.id.navigationLogoutButton) setOnClickListener {
            GCMStation.onLogout()
            VKSdk.logout()
            toLoginActivityAction()
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
        val userPhoto = activity.findViewById(R.id.navigationUserPhoto) as ImageView
        val userName = activity.findViewById(R.id.navigationUserName) as TextView
        val userOnlineStatus = activity.findViewById(R.id.navigationUserOnlineStatus) as TextView

        if (UserCache.getMe() == null) {
            userPhoto setImageResource R.drawable.icon_user_stub
            userName setText ""
            userOnlineStatus setText ""
        } else {
            val me = UserCache.getMe()!!
            ImageLoader.getInstance().displayImage(me.photoUrl, userPhoto)
            userName setText TextFormat.userTitle(me, compact = false)
            userOnlineStatus setText when {
                me.lastOnlineTime == 0L -> ""
                me.isOnline -> TextFormat.userOnlineStatus(me)
                else -> "${TextFormat.lastVisitPhrase(me)} ${TextFormat.lastVisitTime(me)}"
            }
        }
    }

    private fun createCacheListener() = object : DataDepend {
        override fun onDataUpdate() {
            updateMyData()
        }
    }
}