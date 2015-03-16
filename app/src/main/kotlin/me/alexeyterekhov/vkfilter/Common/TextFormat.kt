package me.alexeyterekhov.vkfilter.Common

import me.alexeyterekhov.vkfilter.DataClasses.User

object TextFormat {
    fun userTitle(user: User, compact: Boolean): String {
        if (compact)
            return user.firstName.first() + "." + user.lastName
        else
            return user.firstName + " " + user.lastName
    }
}