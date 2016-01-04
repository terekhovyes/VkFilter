package me.alexeyterekhov.vkfilter.Internet

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestDialogUpdates
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.NotificationHandler
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.NotificationListener

public object DialogRefresher {
    private val DELAY = 1500L
    private var isRunning = false
    private var scheduled = false
    private var dialogId = ""
    private var isChat = false
    private var lastExecutionMillis = 0L

    private val handler = Handler()
    val messageCacheListener = createMessageListener()
    val notificationListener = createNotificationListener()
    private val requestRunnable = {
        newMessagesRequest()
        scheduled = false
    }

    fun start(dialogId: String, isChat: Boolean) {
        this.dialogId = dialogId
        this.isChat = isChat
        isRunning = true
        MessageCaches.getCache(dialogId, isChat).listeners.add(messageCacheListener)
        NotificationHandler.addNotificationListener(notificationListener)
        loopBody()
    }

    fun stop() {
        isRunning = false
        MessageCaches.getCache(dialogId, isChat).listeners.remove(messageCacheListener)
        NotificationHandler.removeNotificationListener(notificationListener)
    }

    fun isRunning() = isRunning

    private fun loopBody() {
        if (isRunning && !scheduled) {
            val cur = System.currentTimeMillis()
            if (cur - lastExecutionMillis < DELAY) {
                handler.postDelayed(requestRunnable, DELAY - cur + lastExecutionMillis)
                scheduled = true
            } else
                newMessagesRequest()
        }
    }

    private fun newMessagesRequest() {
        lastExecutionMillis = System.currentTimeMillis()
        val request = RequestDialogUpdates(dialogId, isChat)
        RequestControl.addForeground(request)
    }

    private fun createMessageListener() = object : MessageCacheListener {
        override fun onAddNewMessages(messages: Collection<Message>) = loopBody()
        override fun onAddOldMessages(messages: Collection<Message>) {}
        override fun onReplaceMessage(old: Message, new: Message) = loopBody()
        override fun onUpdateMessages(messages: Collection<Message>) = loopBody()
        override fun onReadMessages(messages: Collection<Message>) = loopBody()
    }
    private fun createNotificationListener() = object : NotificationListener {
        override fun onNotification(info: NotificationInfo): Boolean {
            return if (info.chatId == "" && !isChat && info.senderId == dialogId ||
                    info.chatId == dialogId && isChat) {
                if (isRunning) {
                    handler.removeCallbacks(requestRunnable)
                    scheduled = false
                    newMessagesRequest()
                }
                true
            } else
                false
        }
    }
}