package me.alexeyterekhov.vkfilter.NotificationService


class NotificationInfo {
    var messageSentId = ""
    var messageText = ""
    var messageSentTime = 0L
    var senderId = ""
    var senderFirstName = ""
    var senderLastName = ""
    var senderPhotoUrl = ""
    var chatId = ""
    var chatTitle = ""
    var chatPhotoUrl = ""

    fun getName(compact: Boolean = false): String {
        return when {
            !compact && chatTitle == "" -> "$senderFirstName $senderLastName"
            compact && chatTitle == "" -> "${senderFirstName.first()}. $senderLastName"
            else -> "${senderFirstName.first()}. $senderLastName ($chatTitle)"
        }
    }

    infix fun canBeReplacedBy(other: NotificationInfo) =
            (sameDialog(other)
            || sameChat(other))
            && other.messageSentTime >= messageSentTime
    private fun sameDialog(other: NotificationInfo) =
            other.senderId == senderId
            && other.chatId == ""
            && chatId == ""
    private fun sameChat(other: NotificationInfo) =
            other.chatId == chatId
            && chatId != ""
}