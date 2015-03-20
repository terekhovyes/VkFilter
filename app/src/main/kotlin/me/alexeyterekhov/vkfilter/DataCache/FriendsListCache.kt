package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataClasses.User
import java.util.Vector


object FriendsListCache {
    public val listeners: Vector<DataDepend> = Vector()

    val list = Vector<User>()
    private var updateTime = 0L

    fun lastUpdate() = updateTime

    fun reloadList(friends: Vector<User>) {
        list.clear()
        addItems(friends)
    }

    fun addItems(friends: Vector<User>) {
        list addAll friends
        updateTime = System.currentTimeMillis()
        for (l in listeners)
            l.onDataUpdate()
    }
}