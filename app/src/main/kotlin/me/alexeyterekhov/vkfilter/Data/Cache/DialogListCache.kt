package me.alexeyterekhov.vkfilter.Data.Cache

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import java.util.*

object DialogListCache {
    var list: List<DialogId> = LinkedList()
    var updateTimeMillis = 0L
}