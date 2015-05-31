package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDependAdapter
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestDialogPartners
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestMessageHistory
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestMessageSend

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
            RequestControl addForeground RequestMessageHistory(
                    dialogId = activity.launchParameters.dialogId(),
                    isChat = activity.launchParameters.isChat(),
                    count = LOAD_PORTION,
                    olderThanId = oldestMessageId
            )
        }
    }

    fun loadLastMessages() {
        if (!messageLoading && !getMessageCache().historyLoaded) {
            messageLoading = true
            getMessageCache().listeners add messageListener
            RequestControl addForeground RequestMessageHistory(
                    dialogId = activity.launchParameters.dialogId(),
                    isChat = activity.launchParameters.isChat(),
                    count = LOAD_PORTION
            )
        }
    }

    fun loadDialogPartners() {
        if (UserCache.getMe() == null)
            RequestControl addBackground RequestDialogPartners(
                    activity.launchParameters.dialogId().toLong(),
                    activity.launchParameters.isChat()
            )
    }

    fun sendMessage(editMessage: Message) {
        val request = RequestMessageSend(
                editMessage,
                activity.launchParameters.dialogId(),
                activity.launchParameters.isChat()
        )
        val guid = request.getSendingGuid()
        RequestControl addBackgroundOrdered request
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