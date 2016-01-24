package me.alexeyterekhov.vkfilter.GUI.DialogsActivityNew.DialogList

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Cache.DialogListCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.User.Device
import me.alexeyterekhov.vkfilter.Data.Utils.DialogUtil
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.GUI.Common.AnimationUtil
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.TextFormatNew
import java.util.*


class DialogAdapter(val list: RecyclerView) : RecyclerView.Adapter<DialogHolder>() {
    private val imageLoader = ImageLoader.getInstance()
    private val filtrator = DialogFiltrator()

    private var filters = DAOFilters.loadVkFilters()
    private var idsUpdateMillis = 0L
    private var idsOriginal: List<DialogId> = LinkedList()
    private var idsFiltered: MutableList<DialogId> = LinkedList()

    override fun getItemCount() = idsFiltered.count()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DialogHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_dialog_circle, parent, false)
        return DialogHolder(view)
    }
    override fun onBindViewHolder(h: DialogHolder, position: Int) {
        val dialogId = idsFiltered[position]
        val dialog = getDialog(dialogId)
        val lastMessage = dialog.messages.last!!

        if (dialog.typingUsers.isNotEmpty()) {
            h.messageText.text = TextFormatNew.typingMessage(dialog.typingUsers)
            h.unreadMessage.visibility = View.INVISIBLE
            h.attachmentIcon.visibility = View.GONE
            h.messageImage.visibility = View.GONE
            h.messageTypingImage.visibility = View.VISIBLE
            AnimationUtil.typingAnimationWhileVisible(h.messageTypingImage)
        } else {
            h.messageText.text = lastMessage.data.text
            h.unreadMessage.visibility = if (lastMessage.isOut && lastMessage.isNotRead) View.VISIBLE else View.INVISIBLE
            h.setAttachmentIcon(lastMessage)
            h.messageTypingImage.visibility = View.GONE

            if (dialogId.isChat) {
                h.messageImage.visibility = View.VISIBLE
                loadImage(h.messageImage, lastMessage.senderOrEmpty().photoUrl)
            }

            if (!dialogId.isChat) {
                h.messageImage.visibility = if (lastMessage.isOut) View.VISIBLE else View.GONE
                if (lastMessage.isOut)
                    loadImage(h.messageImage, lastMessage.senderOrEmpty().photoUrl)
            }
        }

        // Common content
        h.title.text = DialogUtil.title(dialog)
        h.messageDate.text = DateFormat.dialogReceivedDate(lastMessage.sent.timeMillis / 1000L)
        h.unreadBackground.visibility = if (lastMessage.isIn && lastMessage.isNotRead) View.VISIBLE else View.INVISIBLE
        val photoUrls = DialogUtil.photoUrls(dialog).take(4)
        h.chooseImageLayoutForImageCount(photoUrls.size)
        photoUrls.forEachIndexed { pos, url -> loadImage(h.getImageView(pos), url) }

        // Chat content
        if (dialogId.isChat) {
            h.onlineIcon.visibility = View.GONE
        }

        // Dialog content
        if (!dialogId.isChat) {
            h.onlineIcon.visibility = if (DialogUtil.showOnline(dialog)) View.VISIBLE else View.GONE
            h.onlineIcon.setImageResource(when (DialogUtil.onlineDeviceType(dialog)) {
                Device.MOBILE -> R.drawable.icon_online_mobile
                Device.DESKTOP -> R.drawable.icon_online
            })
        }
    }

    private fun updateList() {
        val targetIds = filtrator.filterSnapshot(idsOriginal, filters)

        val man = list.layoutManager as LinearLayoutManager
        val curPos = man.findFirstVisibleItemPosition()
        val top = if (curPos != -1)
            man.findViewByPosition(curPos).top
        else
            0

        // Delete items that not present in new collection
        val positionsToRemove = LinkedList<Int>()
        idsFiltered.forEachIndexed { pos, id ->
            if (!targetIds.contains(id))
                positionsToRemove.add(pos)
        }
        positionsToRemove.reversed().forEach {
            idsFiltered.removeAt(it)
            notifyItemRemoved(it)
        }

        // Add non existing items
        targetIds.forEachIndexed { pos, id ->
            if (!idsFiltered.contains(id)) {
                idsFiltered.add(pos, id)
                notifyItemInserted(pos)
            }
        }

        // Move existing items
        targetIds.forEachIndexed { targetPos, targetId ->
            val realId = idsFiltered[targetPos]
            if (realId != targetId) {
                val realPos = idsFiltered.indexOf(targetId)
                idsFiltered[realPos] = targetId
                notifyItemChanged(realPos)
                idsFiltered.removeAt(realPos)
                idsFiltered.add(targetPos, targetId)
                notifyItemMoved(realPos, targetPos)
            } else {
                idsFiltered[targetPos] = targetId
                notifyItemChanged(targetPos)
            }
        }

        if (curPos != -1)
            man.scrollToPositionWithOffset(curPos, top)
    }

    fun getDialog(pos: Int) = getDialog(idsFiltered[pos])

    fun getDialog(dialogId: DialogId) = DialogCache.getDialogOrCreate(dialogId)

    fun checkForNewDialogs() {
        if (DialogListCache.updateTimeMillis > idsUpdateMillis) {
            idsOriginal = DialogListCache.list
            updateList()
        }
    }

    fun checkForFilters() {
        filters = DAOFilters.loadVkFilters()
        updateList()
    }

    private fun loadImage(view: ImageView, url: String) = imageLoader.displayImage(url, view)
}