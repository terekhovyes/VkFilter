package me.alexeyterekhov.vkfilter.Util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedMessagePack

public object ClipboardUtil {
    private val COPY_TIME_MINUTES = 10

    private var copyTime = 0L
    private var copiedMessages: AttachedMessagePack? = null

    fun putText(text: String) {
        val clipboard = AppContext.instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("vkfilter text", text)
        clipboard.primaryClip = clip
    }

    fun putMessages(pack: AttachedMessagePack) {
        copiedMessages = pack
        copyTime = System.currentTimeMillis()
    }
    fun validateMessages() {
        val time = System.currentTimeMillis()
        if (time - copyTime > COPY_TIME_MINUTES * 60000) {
            copiedMessages = null
        }
    }
    fun getMessages() = copiedMessages
}