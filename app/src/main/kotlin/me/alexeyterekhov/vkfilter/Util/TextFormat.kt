package me.alexeyterekhov.vkfilter.Util

import android.content.Context
import me.alexeyterekhov.vkfilter.DataClasses.Device
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
            user.isOnline -> {
                var status = getStr(R.string.chat_label_toolbar_online)
                if (user.deviceType == Device.MOBILE)
                    status += " ${getStr(R.string.chat_label_toolbar_mobile)}"
                status
            }
            user.lastOnlineTime == 0L -> ""
            else -> {
                var status = "${lastVisitPhrase(user)} ${lastVisitTime(user)}"
                if (user.deviceType == Device.MOBILE)
                    status += " ${getStr(R.string.chat_label_toolbar_mobile)}"
                status
            }
        }
    }

    fun userOnlineStatusCompact(user: User): String = when {
        user.isOnline -> getStr(R.string.chat_label_toolbar_online)
        user.lastOnlineTime == 0L -> ""
        else -> "${lastVisitPhrase(user)} ${lastVisitTime(user)}"
    }

    fun lastVisitPhrase(user: User): String {
        return when (user.sex) {
            Sex.WOMAN -> getStr(R.string.chat_label_toolbar_visit_woman)
            Sex.MAN -> getStr(R.string.chat_label_toolbar_visit_man)
            else -> getStr(R.string.chat_label_toolbar_visit)
        }
    }

    fun membersPhrase(membersCount: Int): String = when (membersCount) {
        1, 21 -> "$membersCount ${getStr(R.string.members_1)}"
        in 2..4, in 22..24 -> "$membersCount ${getStr(R.string.members_2_4)}"
        in 5..20, in 25..30 -> "$membersCount ${getStr(R.string.members_5_more)}"
        else -> "$membersCount ${getStr(R.string.members_5_more)}"
    }

    fun lastVisitTime(user: User) = "${DateFormat.lastOnline(user.lastOnlineTime)}"

    fun andMoreDialogs(context: Context, count: Int): String {
        val str = context.getString(R.string.notification_label_and_more)
        return str.replace("#", count.toString())
    }

    infix fun size(sizeInBytes: Int): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            sizeInBytes < kb -> "$sizeInBytes ${getStr(R.string.size_bytes)}"
            sizeInBytes < mb -> "${sizeInBytes / kb} ${getStr(R.string.size_kbytes)}"
            sizeInBytes < gb -> "${sizeInBytes / mb} ${getStr(R.string.size_mbytes)}"
            else -> "${sizeInBytes / gb} ${getStr(R.string.size_gbytes)}"
        }
    }

    private fun getStr(res: Int) = AppContext.instance.getString(res)
}