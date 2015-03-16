package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import java.util.Vector


public class UserDataItem(
        val id: String,
        val isChat: Boolean
) {
    val photoUrls = Vector<String>()
    var title = ""
    var messageText = ""
    var isSelected = false
}