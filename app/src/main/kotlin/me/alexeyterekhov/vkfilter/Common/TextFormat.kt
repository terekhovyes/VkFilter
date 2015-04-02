package me.alexeyterekhov.vkfilter.Common

import me.alexeyterekhov.vkfilter.DataClasses.Sex
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.R

object TextFormat {
    fun userTitle(user: User, compact: Boolean): String {
        if (compact)
            return user.firstName.first() + "." + user.lastName
        else
            return user.firstName + " " + user.lastName
    }

    fun userOnlineStatus(user: User): String {
        return when {
            user.isOnline -> AppContext.instance.getString(R.string.online)
            user.lastOnlineTime == 0L -> ""
            else -> "${lastVisitPhrase(user)} ${lastVisitTime(user)}"
        }
    }

    fun lastVisitPhrase(user: User): String {
        return when (user.sex) {
            Sex.WOMAN -> AppContext.instance.getString(R.string.last_visit_woman)
            Sex.MAN -> AppContext.instance.getString(R.string.last_visit_man)
            else -> AppContext.instance.getString(R.string.last_visit_common)
        }
    }

    fun lastVisitTime(user: User) = "${DateFormat.lastOnline(user.lastOnlineTime)}"
}