package me.alexeyterekhov.vkfilter.DataClasses

class User {
    var id = ""
    var firstName = ""
    var lastName = ""
    var sex = Sex.UNKNOWN
    var photoUrl: String = ""
    var isOnline: Boolean = false
    var lastOnlineTime: Long = 0
    var onlineStatusChanged: Boolean = false
}