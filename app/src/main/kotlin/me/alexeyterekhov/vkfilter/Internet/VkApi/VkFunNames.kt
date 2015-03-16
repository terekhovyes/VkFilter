package me.alexeyterekhov.vkfilter.Internet.VkApi

import kotlin.properties.Delegates

public object VkFunNames {
    private val names by Delegates.lazy {
        hashMapOf(
                VkFun.dialogList to "execute.detailedDialogs",
                VkFun.messageList to "messages.getHistory",
                VkFun.friendList to "friends.get",
                VkFun.markIncomesAsRead to "messages.markAsRead",
                VkFun.refreshDialog to "execute.refreshDialog",
                VkFun.sendMessage to "messages.send",
                VkFun.registerGCM to "account.registerDevice",
                VkFun.userInfo to "users.get"
        )
    }

    public fun name(vkFun: VkFun): String {
        return names[vkFun]
    }
}