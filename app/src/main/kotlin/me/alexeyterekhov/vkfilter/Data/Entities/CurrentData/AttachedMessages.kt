package me.alexeyterekhov.vkfilter.Data.Entities.CurrentData

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId

class AttachedMessages(
        val title: String,
        val messageIds: List<Long>,
        val dialogId: DialogId
)