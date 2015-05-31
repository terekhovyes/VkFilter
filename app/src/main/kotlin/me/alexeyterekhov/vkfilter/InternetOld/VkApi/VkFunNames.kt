package me.alexeyterekhov.vkfilter.InternetOld.VkApi

import kotlin.properties.Delegates

public object VkFunNames {
    private val names by Delegates.lazy {
        hashMapOf(
                VkFun.dialogList to "execute.detailedDialogs",
                VkFun.messageList to "messages.getHistory",
                VkFun.friendList to "friends.get",
                VkFun.markIncomesAsReadOld to "messages.markAsRead",
                VkFun.markIncomesAsRead to "messages.markAsRead",
                VkFun.refreshDialog to "execute.refreshDialog",
                VkFun.sendMessage to "messages.send",
                VkFun.sendMessageOld to "messages.send",
                VkFun.registerGCM to "account.registerDevice",
                VkFun.unregisterGCM to "account.unregisterDevice",
                VkFun.userInfo to "users.get",
                VkFun.chatInfo to "execute.detailedChats",
                VkFun.notificationInfo to "execute.notificationInfo",
                VkFun.getDialogPartners to "execute.getDialogPartners",
                VkFun.videoUrls to "execute.videoUrls"
        )
    }

    public fun name(vkFun: VkFun): String {
        return names[vkFun]
    }
}