package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Util.TextFormat

class ActionBarModule(val activity: ChatActivity) {
    val listener = createListener()

    fun onCreate() {
        activity.setTitle(activity.launchParameters.windowTitle())
        activity.getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
    }
    fun onResume() {
        if (activity.launchParameters.isNotChat()) {
            updateSubtitle()
            UserCache.listeners add listener
        }
    }
    fun onPause() {
        if (activity.launchParameters.isNotChat())
            UserCache.listeners remove listener
    }

    private fun updateSubtitle() {
        if (activity.launchParameters.isNotChat())
            if (UserCache.contains(activity.launchParameters.dialogId())) {
                val user = UserCache.getUser(activity.launchParameters.dialogId())!!
                activity.getSupportActionBar().setSubtitle(TextFormat.userOnlineStatus(user))
            }
    }
    private fun createListener() = object : DataDepend {
        override fun onDataUpdate() {
            updateSubtitle()
        }
    }
}