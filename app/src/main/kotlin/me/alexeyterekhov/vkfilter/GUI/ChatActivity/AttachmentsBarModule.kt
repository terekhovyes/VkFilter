package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedImages
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList.AttachmentsAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


class AttachmentsBarModule(val activity: ChatActivity): AttachedImages.AttachedImageListener {
    fun onCreate() {
        val dialogId = activity.launchParameters.dialogId()
        val isChat = activity.launchParameters.isChat()
        val list = findRecycler()
        if (list.getAdapter() == null) {
            val adapter = AttachmentsAdapter(list, dialogId, isChat)
            adapter.data addAll AttachedCache.get(dialogId, isChat).images.uploads
            list.setAdapter(adapter)
            list.setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
            list.setItemAnimator(DefaultItemAnimator())
        }

        with (AttachedCache.get(dialogId, isChat).images.listeners) {
            add(list.getAdapter() as AttachmentsAdapter)
            add(this@AttachmentsBarModule)
        }

        checkVisibility()
    }

    fun onDestroy() {
        val dialogId = activity.launchParameters.dialogId()
        val isChat = activity.launchParameters.isChat()

        with (AttachedCache.get(dialogId, isChat).images.listeners) {
            remove(findRecycler().getAdapter() as AttachmentsAdapter)
            remove(this@AttachmentsBarModule)
        }
    }

    fun findRecycler() = activity.findViewById(R.id.attachmentsBar) as RecyclerView

    override fun onRemoved(image: ImageUpload) = checkVisibility()
    override fun onAdd(image: ImageUpload) = checkVisibility()

    private fun checkVisibility() {
        val dialogId = activity.launchParameters.dialogId()
        val isChat = activity.launchParameters.isChat()
        val r = findRecycler()
        if (AttachedCache.get(dialogId, isChat).images.uploads.isEmpty()) {
            if (r.getVisibility() == View.VISIBLE)
                r.setVisibility(View.GONE)
        } else {
            if (r.getVisibility() != View.VISIBLE)
                r.setVisibility(View.VISIBLE)
        }
    }
}