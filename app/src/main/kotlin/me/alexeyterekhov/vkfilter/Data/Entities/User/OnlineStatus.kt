package me.alexeyterekhov.vkfilter.Data.Entities.User

class OnlineStatus : Cloneable {
    var isOnline = false
    var isOffline: Boolean
        get() = !isOnline
        set(value) { isOnline = !value }
    var lastVisitTime = 0L
    var device = Device.DESKTOP

    override public fun clone(): Any {
        val copy = OnlineStatus()

        copy.isOnline = isOnline
        copy.lastVisitTime = lastVisitTime
        copy.device = device

        return copy
    }
}