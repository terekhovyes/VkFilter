package me.alexeyterekhov.vkfilter.DataCache.Common

import java.util.*


infix fun <T> List<T>.forEachSync(action: (T) -> Unit) {
    val copy = LinkedList(this)
    copy.forEach { action(it) }
}