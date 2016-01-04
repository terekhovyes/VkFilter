package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedImages
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList.AttachmentsAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


class AttachmentsBarModule(val activity: ChatActivity):
        AttachedImages.AttachedImageListener,
        DataDepend
{
    fun onCreate() {
        val dialogId = activity.launchParameters.dialogId()
        val isChat = activity.launchParameters.isChat()
        val list = findRecycler()
        if (list.adapter == null) {
            val adapter = AttachmentsAdapter(list)
            adapter setData AttachedCache.get(dialogId, isChat)
            list.adapter = adapter
            list.layoutManager = LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false)
            list.itemAnimator = DefaultItemAnimator()
        }

        with (AttachedCache.get(dialogId, isChat)) {
            val adapter = list.adapter as AttachmentsAdapter
            images.listeners.add(adapter.uploadListener)
            images.listeners.add(this@AttachmentsBarModule)
            listeners.add(adapter.dataListener)
            listeners.add(this@AttachmentsBarModule)
        }

        checkVisibility()
    }

    fun onDestroy() {
        val dialogId = activity.launchParameters.dialogId()
        val isChat = activity.launchParameters.isChat()

        with (AttachedCache.get(dialogId, isChat)) {
            val adapter = findRecycler().adapter as AttachmentsAdapter
            images.listeners.remove(adapter.uploadListener)
            images.listeners.remove(this@AttachmentsBarModule)
            listeners.remove(adapter.dataListener)
            listeners.remove(this@AttachmentsBarModule)
        }
    }

    fun findRecycler() = activity.findViewById(R.id.attachmentsBar) as RecyclerView

    override fun onRemoved(image: ImageUpload) = checkVisibility()
    override fun onAdd(image: ImageUpload) = checkVisibility()
    override fun onDataUpdate() = checkVisibility()

    private fun checkVisibility() {
        val cache = AttachedCache.get(
                activity.launchParameters.dialogId(),
                activity.launchParameters.isChat()
        )
        val messageCount = cache.messages.get().count()
        val imageCount = cache.images.uploads.count()

        val r = findRecycler()
        if (messageCount + imageCount == 0) {
            if (r.visibility == View.VISIBLE)
                r.visibility = View.GONE
        } else {
            if (r.visibility != View.VISIBLE)
                r.visibility = View.VISIBLE
        }
    }
}