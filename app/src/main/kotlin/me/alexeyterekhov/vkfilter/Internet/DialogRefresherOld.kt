package me.alexeyterekhov.vkfilter.Internet

import android.os.Handler
import com.vk.sdk.api.VKParameters
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.MessageCacheOld
import me.alexeyterekhov.vkfilter.DataClasses.MessageOld
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestBundle
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.NotificationListener
import java.util.NoSuchElementException

object DialogRefresherOld : DataDepend, NotificationListener {
    private val DELAY = 1500L
    private var running = false
    private var scheduled = false
    private var id = ""
    private var chat = false
    private var lastRun = 0L

    private val handler = Handler()
    private val updateRunnable = {
        request()
        scheduled = false
    }

    fun start(id: String, isChat: Boolean) {
        $id = id
        chat = isChat
        running = true
        MessageCacheOld.getDialog(id, isChat).listeners add this
        GCMStation.addNotificationListener(this)
        run()
    }

    fun stop() {
        running = false
        MessageCacheOld.getDialog(id, chat).listeners remove this
        GCMStation.removeNotificationListener(this)
    }

    override fun onDataUpdate() {
        run()
    }

    override fun onNotification(info: NotificationInfo): Boolean {
        return if (info.chatId == "" && !chat && info.senderId == id ||
                    info.chatId == id && chat) {
            runFromIntent()
            true
        } else
            false
    }

    private fun runFromIntent() {
        if (running) {
            handler removeCallbacks updateRunnable
            scheduled = false
            request()
        }
    }

    private fun run() {
        if (running && !scheduled) {
            val cur = System.currentTimeMillis()
            if (cur - lastRun < DELAY) {
                handler.postDelayed(updateRunnable, DELAY - cur + lastRun)
                scheduled = true
            } else {
                request()
            }
        }
    }

    private fun request() {
        lastRun = System.currentTimeMillis()
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.refreshDialog, createParams()))
    }

    private fun createParams(): VKParameters {
        val params = VKParameters()
        params[if (chat) "chat_id" else "user_id"] = id
        val messagePack = MessageCacheOld.getDialog(id, chat)
        params["last_id"] =
                if (messagePack.messages.isNotEmpty())
                    messagePack.messages.last().id
                else
                    0
        var lastRead: MessageOld?
        try {
            lastRead = messagePack.messages last { it.isOut && it.isRead }
        } catch (e: NoSuchElementException) {
            lastRead = null
        }
        params["read_id"] = if (lastRead == null) 0 else lastRead!!.id
        return params
    }
}