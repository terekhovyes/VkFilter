package me.alexeyterekhov.vkfilter.Data.UpdateEvents

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId

class EventDialogListIncreased(val newDialogs: List<DialogId>, val offset: Int)