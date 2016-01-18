package me.alexeyterekhov.vkfilter.Data.Entities.User

class User : Cloneable {
    var id = ""
    var firstName = ""
    var lastName = ""
    var sex = Sex.UNKNOWN
    var photoUrl = ""
    var onlineStatus = OnlineStatus()

    override public fun clone(): Any {
        val copy = User()

        copy.id = id
        copy.firstName = firstName
        copy.lastName = lastName
        copy.sex = sex
        copy.photoUrl = photoUrl
        copy.onlineStatus = onlineStatus.clone() as OnlineStatus

        return copy
    }
}