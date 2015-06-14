package me.alexeyterekhov.vkfilter.DataCache.Common

import java.util.LinkedList


fun <T> LinkedList<T>.forEachSync(action: (T) -> Unit) {
    val copy = LinkedList(this)
    copy forEach { action(it) }
}