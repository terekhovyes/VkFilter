package me.alexeyterekhov.vkfilter.Util

import me.alexeyterekhov.vkfilter.Data.Entities.User.User

object TextFormatNew {
    fun userTitle(user: User, compact: Boolean = false): String {
        if (compact)
            return user.firstName.first() + "." + user.lastName
        else
            return user.firstName + " " + user.lastName
    }
}