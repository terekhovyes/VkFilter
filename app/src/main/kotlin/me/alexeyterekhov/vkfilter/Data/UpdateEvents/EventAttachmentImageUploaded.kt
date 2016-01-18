package me.alexeyterekhov.vkfilter.Data.UpdateEvents

import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId

class EventAttachmentImageUploaded(val dialogId: DialogId, val image: AttachedImage)