package me.alexeyterekhov.vkfilter.Util

import me.alexeyterekhov.vkfilter.Data.Entities.User.User
import me.alexeyterekhov.vkfilter.R

object TextFormatNew {
    fun userTitle(user: User, compact: Boolean = false): String {
        if (compact)
            return user.firstName.first() + "." + user.lastName
        else
            return user.firstName + " " + user.lastName
    }

    fun typingMessage(users: Collection<User>): String {
        val postfix = AppContext.instance.getString(if (users.size == 1)
                    R.string.chat_label_typing
                else
                    R.string.chat_label_typing_many)
        return users
                .map { it.firstName }
                .joinToString(separator = ", ", postfix = " $postfix")
    }
}