package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data

import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.User
import java.util.Vector

class Dialog {
    private val chatPartners: Vector<User> = Vector()

    public var id: Long = 0
    public var photoUrl: String = ""
    public var lastMessage: Message? = null
    public var title: String = ""
        get() {
            if ($title != "")
                return $title
            if (chatPartners.size == 1)
                return TextFormat.userTitle(chatPartners.first!!, false)
            val names = StringBuilder()
            val to = if (chatPartners.size > 4) 4 else chatPartners.size
            for (i in 0..to - 1) {
                if (i != 0)
                    names.append(", ": CharSequence)
                names.append(TextFormat.userTitle(chatPartners[i], true): CharSequence)
            }
            if (chatPartners.size > 4)
                names.append("...": CharSequence)
            return names.toString()
        }

    public fun addPartner(user: User): Unit {
        chatPartners.add(user)
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

    public fun getPartnerPhotoUrl(pos: Int): String? {
        if (pos < 0 || pos >= chatPartners.size) return null
        return chatPartners[pos].photoUrl
    }

    public fun getPartnersCount(): Int = chatPartners.size

    public fun isChat(): Boolean = chatPartners.size > 1

    public fun showOnlineIcon(): Boolean = chatPartners.size == 1 && chatPartners[0].isOnline
}