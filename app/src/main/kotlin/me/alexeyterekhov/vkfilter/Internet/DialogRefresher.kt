package me.alexeyterekhov.vkfilter.Internet

import android.content.Intent
import android.os.Handler
import com.vk.sdk.api.VKParameters
import me.alexeyterekhov.vkfilter.Common.IntentListener
import me.alexeyterekhov.vkfilter.Common.ReceiverStation
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.MessageCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestBundle
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import java.util.NoSuchElementException

object DialogRefresher: DataDepend, IntentListener {
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
        MessageCache.getDialog(id, isChat).listeners add this
        ReceiverStation.listener = this
        run()
    }

    fun stop() {
        running = false
        MessageCache.getDialog(id, chat).listeners remove this
        if (ReceiverStation.listener == this)
            ReceiverStation.listener = null
    }

    override fun onDataUpdate() {
        run()
    }

    override fun onGetIntent(intent: Intent) {
        runFromIntent()
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
        VkRequestControl.addRequest(VkRequestBundle(VkFun.refreshDialog, createParams()))
    }

    private fun createParams(): VKParameters {
        val params = VKParameters()
        params[if (chat) "chat_id" else "user_id"] = id
        val messagePack = MessageCache.getDialog(id, chat)
        params["last_id"] =
                if (messagePack.messages.notEmpty)
                    messagePack.messages.last!!.id
                else
                    0
        var lastRead: Message?
        try {
            lastRead = messagePack.messages last { it.isOut && it.isRead }
        } catch (e: NoSuchElementException) {
            lastRead = null
        }
        params["read_id"] = if (lastRead == null) 0 else lastRead!!.id
        return params
    }
}