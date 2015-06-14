package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataClasses.User
import java.util.HashMap
import java.util.Vector

object UserCache {
    private val myId = "me"
    private val map = HashMap<String, User>()

    public val listeners: Vector<DataDepend> = Vector()

    public fun dataUpdated() {
        for (l in listeners)
            l.onDataUpdate()
    }
    public fun putUser(user: User) {
        map[user.id] = user
    }
    public fun contains(id: String): Boolean = map.containsKey(id)
    public fun getUser(id: String): User? = map[id]
    public fun getMe(): User? = map[myId]
    public fun getMyId(): String = myId
}