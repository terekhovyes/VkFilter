package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataClasses.User
import java.util.HashMap
import java.util.Vector
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend

object UserCache {
    private val map = HashMap<String, User>()

    public val listeners: Vector<DataDepend> = Vector<DataDepend>()

    public fun dataUpdated() {
        for (l in listeners)
            l.onDataUpdate()
    }
    public fun putUser(id: String, user: User) {
        map[id] = user
    }
    public fun contains(id: String): Boolean = map.containsKey(id)
    public fun getUser(id: String): User? = map[id]
}