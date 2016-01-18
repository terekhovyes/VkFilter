package me.alexeyterekhov.vkfilter.Data.UpdateEvents

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message

class EventMessagesAddOld(val dialogId: DialogId, val newMessages: List<Message>)