package me.alexeyterekhov.vkfilter.Data.Cache

import me.alexeyterekhov.vkfilter.Data.Entities.User.User
import java.util.*

object UserCache {
    private val myId = "me"
    private val map = HashMap<String, User>()

    fun putUser(user: User) {
        map[user.id] = user
    }
    fun contains(id: String): Boolean = map.containsKey(id)
    fun getUser(id: String): User? = map[id]
    fun getMe(): User? = map[myId]
    fun getMyId(): String = myId
}