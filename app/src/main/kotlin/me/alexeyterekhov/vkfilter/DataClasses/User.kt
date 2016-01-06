package me.alexeyterekhov.vkfilter.DataClasses

class User {
    var id = ""
    var firstName = ""
    var lastName = ""
    var sex = Sex.UNKNOWN
    var photoUrl: String = ""
    var isOnline: Boolean = false
    var lastOnlineTime: Long = 0
    var deviceType: Device = Device.DESKTOP

    fun copy(): User {
        val copy = User()
        copy.id = id
        copy.firstName = firstName
        copy.lastName = lastName
        copy.sex = sex
        copy.photoUrl = photoUrl
        copy.isOnline = isOnline
        copy.lastOnlineTime = lastOnlineTime
        copy.deviceType = deviceType
        return copy
    }
}