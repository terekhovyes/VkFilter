package me.alexeyterekhov.vkfilter.Common

import android.content.Context
import me.alexeyterekhov.vkfilter.DataClasses.Sex
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.R

object TextFormat {
    fun userTitle(user: User, compact: Boolean = false): String {
        if (compact)
            return user.firstName.first() + "." + user.lastName
        else
            return user.firstName + " " + user.lastName
    }

    fun userOnlineStatus(user: User): String {
        return when {
            user.isOnline -> AppContext.instance.getString(R.string.a_chat_online)
            user.lastOnlineTime == 0L -> ""
            else -> "${lastVisitPhrase(user)} ${lastVisitTime(user)}"
        }
    }

    fun lastVisitPhrase(user: User): String {
        return when (user.sex) {
            Sex.WOMAN -> AppContext.instance.getString(R.string.a_chat_last_visit_woman)
            Sex.MAN -> AppContext.instance.getString(R.string.a_chat_last_visit_man)
            else -> AppContext.instance.getString(R.string.a_chat_last_visit_common)
        }
    }

    fun lastVisitTime(user: User) = "${DateFormat.lastOnline(user.lastOnlineTime)}"

    fun newDialogs(context: Context, count: Int): String {
        return when (count) {
            1 -> "$count ${context.getString(R.string.new_dialog_1)}"
            in 2..4 -> "$count ${context.getString(R.string.new_dialog_2_4)}"
            in 5..20 -> "$count ${context.getString(R.string.new_dialog_5_0)}"
            else -> when (count % 10) {
                1 -> "$count ${context.getString(R.string.new_dialog_1)}"
                in 2..4 -> "$count ${context.getString(R.string.new_dialog_2_4)}"
                in 5..9, 0 -> "$count ${context.getString(R.string.new_dialog_5_0)}"
                else -> "${context.getString(R.string.new_dialog)} $count"
            }
        }
    }

    fun andMoreDialogs(context: Context, count: Int): String {
        val str = context.getString(R.string.plus_more)
        return str.replace("#", count.toString())
    }

    fun size(sizeInBytes: Int): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            sizeInBytes < kb -> "$sizeInBytes ${AppContext.instance.getString(R.string.bytes)}"
            sizeInBytes < mb -> "${sizeInBytes / kb} ${AppContext.instance.getString(R.string.kbytes)}"
            sizeInBytes < gb -> "${sizeInBytes / mb} ${AppContext.instance.getString(R.string.mbytes)}"
            else -> "${sizeInBytes / gb} ${AppContext.instance.getString(R.string.gbytes)}"
        }
    }
}