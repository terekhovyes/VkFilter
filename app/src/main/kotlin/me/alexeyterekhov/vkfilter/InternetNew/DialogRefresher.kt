package me.alexeyterekhov.vkfilter.InternetNew

import android.os.Handler
import com.vk.sdk.api.VKParameters
import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
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
        MessageCaches.getCache(dialogId, isChat).listeners add messageCacheListener
        GCMStation addNotificationListener notificationListener
        loopBody()
    }

    fun stop() {
        isRunning = false
        MessageCaches.getCache(dialogId, isChat).listeners remove messageCacheListener
        GCMStation removeNotificationListener notificationListener
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
        val params = VKParameters()
        val messages = MessageCaches.getCache(dialogId, isChat).getMessages()
        if (isChat)
            params["chat_id"] = dialogId
        else
            params["user_id"] = dialogId
        params["last_id"] = if (messages.isNotEmpty())
            messages.last { it.sentState == Message.STATE_SENT } .sentId
        else
            0L
        val lastMessage = messages.lastOrNull { it.sentState == Message.STATE_SENT && it.isOut && it.isRead }
        params["read_id"] = lastMessage?.sentId ?: 0L
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.refreshDialog, params))
    }

    private fun createMessageListener() = object : MessageCacheListener {
        override fun onAddNewMessages(count: Int) = loopBody()
        override fun onAddOldMessages(count: Int) {}
        override fun onReplaceMessage(old: Message, new: Message) = loopBody()
        override fun onUpdateMessages(messages: Collection<Message>) = loopBody()
        override fun onReadMessages(messages: Collection<Message>) = loopBody()
    }
    private fun createNotificationListener() = object : NotificationListener {
        override fun onNotification(info: NotificationInfo): Boolean {
            return if (info.chatId == "" && !isChat && info.senderId == dialogId ||
                    info.chatId == dialogId && isChat) {
                if (isRunning) {
                    handler removeCallbacks requestRunnable
                    scheduled = false
                    newMessagesRequest()
                }
                true
            } else
                false
        }
    }
}