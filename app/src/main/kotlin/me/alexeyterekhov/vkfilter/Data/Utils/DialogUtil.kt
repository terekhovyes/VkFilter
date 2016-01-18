package me.alexeyterekhov.vkfilter.Data.Utils

import android.text.TextUtils
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.Dialog
import me.alexeyterekhov.vkfilter.Data.Entities.User.Device
import me.alexeyterekhov.vkfilter.Util.TextFormatNew
import java.util.*

object DialogUtil {
    fun title(dialog: Dialog): String {
        if (!TextUtils.isEmpty(dialog.specialTitle))
            return dialog.specialTitle
        if (dialog.partners.count() == 1)
            return TextFormatNew.userTitle(dialog.partners.first(), compact = false)
        return dialog.partners
            .take(if (dialog.partners.count() > 4) 4 else dialog.partners.count())
            .map { TextFormatNew.userTitle(it, compact = true) }
            .joinToString(
                    separator = ", ",
                    postfix = if (dialog.partners.count() > 4) "..." else "")
    }

    fun photoCount(dialog: Dialog) = when {
        !TextUtils.isEmpty(dialog.specialPhotoUrl) -> 1
        else -> Math.min(dialog.partners.count(), 4)
    }

    fun photoUrl(dialog: Dialog, pos: Int) = when {
        !TextUtils.isEmpty(dialog.specialPhotoUrl) -> dialog.specialPhotoUrl
        else -> dialog.partners[pos].photoUrl
    }

    fun photoUrls(dialog: Dialog): List<String> {
        val list = LinkedList<String>()

        for (pos in 0..photoCount(dialog) - 1)
            list.add(photoUrl(dialog, pos))

        return list
    }

    fun showOnline(dialog: Dialog) = dialog.partners.count() == 1 && dialog.partners.first().onlineStatus.isOnline

    fun onlineDeviceType(dialog: Dialog) = if (dialog.partners.isEmpty()) Device.DESKTOP else dialog.partners.first().onlineStatus.device

    fun equals(d1: Dialog, d2: Dialog): Boolean {

        return d1.id == d2.id
                && d1.specialPhotoUrl == d2.specialPhotoUrl
                && title(d1) == title(d2)
                && photosEquals(d1, d2)
                && typingEquals(d1, d2)
                && showOnline(d1) == showOnline(d2)
                && (!showOnline(d1) || showOnline(d1) && onlineDeviceType(d1) == onlineDeviceType(d2))
                && lastMessagesEquals(d1, d2)
                && samePartners(d1, d2)
    }

    fun mergeDialogInformation(old: Dialog, new: Dialog): Dialog {
        val out = old.clone() as Dialog

        out.id = new.id
        if (new.partners.isNotEmpty())
            out.partners = new.partners
        if (!TextUtils.isEmpty(new.specialPhotoUrl))
            out.specialPhotoUrl = new.specialPhotoUrl
        if (!TextUtils.isEmpty(new.specialTitle))
            out.specialTitle = new.specialTitle

        return out
    }

    private fun photosEquals(d1: Dialog, d2: Dialog): Boolean {
        val photoUrls1 = photoUrls(d1)
        val photoUrls2 = photoUrls(d2)
        return photoCount(d1) == photoCount(d2)
                && photoUrls1.all { photoUrls2.contains(it) }
    }

    private fun typingEquals(d1: Dialog, d2: Dialog): Boolean {
        return d1.typingUsers.count() == d2.typingUsers.count()
                && d1.typingUsers.all { u1 -> d2.typingUsers.any { it.id == u1.id } }
    }

    private fun lastMessagesEquals(d1: Dialog, d2: Dialog): Boolean {
        val m1 = d1.messages.last
        val m2 = d2.messages.last

        return m1 == null && m2 == null
                || m1 != null && m2 != null
                && m1.sent.state == m2.sent.state
                && m1.sent.id == m2.sent.id
                && m1.data.text == m2.data.text
                && m1.isRead == m2.isRead
    }

    private fun samePartners(d1: Dialog, d2: Dialog): Boolean {
        return d1.partners.count() == d2.partners.count()
                && d1.partners.all { user -> d2.partners.any {
                    it.id == user.id && it.onlineStatus.isOnline == user.onlineStatus.isOnline && it.photoUrl == user.photoUrl
                } }
    }
}