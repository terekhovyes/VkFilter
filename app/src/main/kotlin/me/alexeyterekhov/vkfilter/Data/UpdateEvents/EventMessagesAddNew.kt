package me.alexeyterekhov.vkfilter.Data.UpdateEvents

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message

class EventMessagesAddNew(val dialogId: DialogId, val newMessages: List<Message>)