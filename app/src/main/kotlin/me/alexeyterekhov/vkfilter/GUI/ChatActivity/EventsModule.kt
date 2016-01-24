package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import me.alexeyterekhov.vkfilter.DataCache.TypingCache
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl

class EventsModule(val activity: ChatActivity) {
    private val typingCacheListener = createTypingListener()

    fun onResume() {
        TypingCache.listeners.add(typingCacheListener)
        LongPollControl.start()
    }

    fun onPause() {
        LongPollControl.stop()
        TypingCache.listeners.remove(typingCacheListener)
    }

    private fun createTypingListener() = object : TypingCache.TypingListener {
        override fun onStartTyping(dialogId: String, isChat: Boolean, userId: String) {
            if (dialogId == activity.launchParameters.dialogId()
                    && isChat == activity.launchParameters.isChat())
                activity.listModule.checkTypingCache()
        }

        override fun onStopTyping(dialogId: String, isChat: Boolean, userId: String) {
            if (dialogId == activity.launchParameters.dialogId()
                    && isChat == activity.launchParameters.isChat())
                activity.listModule.checkTypingCache()
        }
    }
}