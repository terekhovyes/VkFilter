package me.alexeyterekhov.vkfilter.NotificationService


class NotificationInfo {
    var messageId = ""
    var text = ""
    var date = 0L
    var senderId = ""
    var firstName = ""
    var lastName = ""
    var senderPhotoUrl = ""
    var chatId = ""
    var chatTitle = ""
    var chatPhotoUrl = ""

    fun getName(compact: Boolean = false): String {
        return when {
            !compact && chatTitle == "" -> "$firstName $lastName"
            compact && chatTitle == "" -> "${firstName.first()}. $lastName"
            else -> "${firstName.first()}. $lastName ($chatTitle)"
        }
    }

    fun canBeReplacedBy(other: NotificationInfo) =
            (sameDialog(other)
            || sameChat(other))
            && other.date >= date
    private fun sameDialog(other: NotificationInfo) =
            other.senderId == senderId
            && other.chatId == ""
            && chatId == ""
    private fun sameChat(other: NotificationInfo) =
            other.chatId == chatId
            && chatId != ""
}