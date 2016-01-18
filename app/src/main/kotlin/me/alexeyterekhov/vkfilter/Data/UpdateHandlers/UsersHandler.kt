package me.alexeyterekhov.vkfilter.Data.UpdateHandlers

import me.alexeyterekhov.vkfilter.Data.Cache.FriendListCache
import me.alexeyterekhov.vkfilter.Data.Cache.UserCache
import me.alexeyterekhov.vkfilter.Data.Entities.User.User
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventFriendListIncreased
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventFriendListReloaded
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventUsersUpdated
import me.alexeyterekhov.vkfilter.Util.EventBuses
import java.util.*

class UsersHandler {
    fun updateUsers(users: Collection<User>, postEvent: Boolean = true) {
        users.forEach { UserCache.putUser(it) }
        if (postEvent)
            postUsersUpdated(users)
    }

    fun updateFriendList(users: List<User>, offset: Int) {
        if (offset == 0) {
            FriendListCache.list = users
            EventBuses.dataBus().post(EventFriendListReloaded(users))
        } else {
            val mergedList = Vector(FriendListCache.list)
            mergedList.addAll(users)
            FriendListCache.list = mergedList
            EventBuses.dataBus().post(EventFriendListIncreased(users))
        }
    }

    fun postUsersUpdated(users: Collection<User>) = EventBuses.dataBus().post(EventUsersUpdated(users))
}