package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.Util.TextFormat
import java.util.*


class ChatInfo {
    val chatPartners: Vector<User> = Vector()
    var id: Long = 0
    var photoUrl: String = ""
    var title: String = ""
        get() {
            if (field != "")
                return field
            if (chatPartners.size == 1)
                return TextFormat.userTitle(chatPartners.first()!!, false)
            val names = StringBuilder()
            val to = if (chatPartners.size > 4) 4 else chatPartners.size
            for (i in 0..to - 1) {
                if (i != 0)
                    names.append(", ")
                names.append(TextFormat.userTitle(chatPartners[i], true))
            }
            if (chatPartners.size > 4)
                names.append("...")
            return names.toString()
        }

    public fun getImageCount(): Int {
        val count = if (photoUrl != "") 1 else chatPartners.size
        return if (count < 5) count else 4
    }

    public fun getImageUrl(pos: Int): String {
        if (photoUrl != "")
            return photoUrl
        return chatPartners[pos].photoUrl
    }
}