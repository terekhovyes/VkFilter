package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDependAdapter
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.MessageNew
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun

class RequestModule(val activity: ChatActivity) {
    companion object {
        val LOAD_PORTION = 40
        val LOAD_THRESHOLD = 20
    }

    var messageLoading = false
    val messageListener = createCacheListener()

    fun loadMessagesOlderThan(oldestMessageId: String) {
        if (!messageLoading && !getMessageCache().historyLoaded) {
            messageLoading = true
            getMessageCache().listeners add messageListener
            RunFun.messageList(
                    dialogId = activity.launchParameters.dialogId(),
                    dialogIsChat = activity.launchParameters.isChat(),
                    offset = 0,
                    count = LOAD_PORTION,
                    startMessageId = oldestMessageId
            )
        }
    }

    fun loadLastMessages() {
        if (!messageLoading && !getMessageCache().historyLoaded) {
            messageLoading = true
            getMessageCache().listeners add messageListener
            RunFun.messageList(
                    dialogId = activity.launchParameters.dialogId(),
                    dialogIsChat = activity.launchParameters.isChat(),
                    offset = 0,
                    count = LOAD_PORTION,
                    startMessageId = ""
            )
        }
    }

    fun loadDialogPartners() {
        if (UserCache.getMe() == null)
            RunFun.getDialogPartners(
                    activity.launchParameters.dialogId().toLong(),
                    activity.launchParameters.isChat()
            )
    }

    fun sendMessage(editMessage: MessageNew) {
        val guid = RunFun.sendMessage(
                editMessage,
                activity.launchParameters.dialogId(),
                activity.launchParameters.isChat()
        )
        getMessageCache().onWillSendMessage(guid)
    }

    private fun getMessageCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )

    private fun createCacheListener() = object : DataDependAdapter() {
        override fun onDataUpdate() {
            getMessageCache().listeners remove this
            messageLoading = false
        }
    }
}