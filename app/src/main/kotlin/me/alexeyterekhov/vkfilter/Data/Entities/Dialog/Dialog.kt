package me.alexeyterekhov.vkfilter.Data.Entities.Dialog

import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.CurrentData
import me.alexeyterekhov.vkfilter.Data.Entities.User.User
import java.util.*

class Dialog : Cloneable {
    var id: DialogId = DialogId(0L, false)
    var partners: List<User> = LinkedList()
    var messages = DialogMessages()
    var specialTitle = ""
    var specialPhotoUrl = ""
    var typingUsers: Collection<User> = LinkedList()
    var current = CurrentData()

    override public fun clone(): Any {
        val copy = Dialog()

        copy.partners = copy(partners)
        copy.messages = messages.clone() as DialogMessages
        copy.specialTitle = specialTitle
        copy.specialPhotoUrl = specialPhotoUrl
        copy.typingUsers = copy(typingUsers)
        copy.current = current

        return copy
    }

    private fun <Type> copy(list: Collection<Cloneable>): List<Type> {
        val copy = Vector<Type>()
        list.forEach { copy.add(it.clone() as Type) }
        return copy
    }
}