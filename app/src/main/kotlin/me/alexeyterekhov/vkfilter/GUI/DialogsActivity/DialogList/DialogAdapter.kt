package me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogList

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataClasses.Device
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import java.util.*


class DialogAdapter(val list: RecyclerView) : RecyclerView.Adapter<DialogHolder>() {
    private val imageLoader = ImageLoader.getInstance()
    private val filtrator = DialogFiltrator()
    private var snapshot = DialogListCache.emptySnapshot()
    private var filters = DAOFilters.loadVkFilters()
    private var filteredDialogs = Vector<Dialog>()

    override fun getItemCount() = filteredDialogs.count()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DialogHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_dialog_circle, parent, false)
        return DialogHolder(view)
    }
    override fun onBindViewHolder(h: DialogHolder, position: Int) {
        val dialog = filteredDialogs.get(position)
        val lastMessage = dialog.lastMessage!!

        if (!TextUtils.isEmpty(dialog.activityMessage)) {
            h.messageText.text = dialog.activityMessage
            h.unreadMessage.visibility = View.INVISIBLE
            h.attachmentIcon.visibility = View.GONE
            h.messageImage.visibility = View.GONE
            h.messageText.setTextColor(AppContext.instance.getColor(R.color.m_olive))
            h.messageTypingImage.visibility = View.VISIBLE
        } else {
            h.messageText.text = lastMessage.text
            h.unreadMessage.visibility = if (lastMessage.isOut && lastMessage.isNotRead) View.VISIBLE else View.INVISIBLE
            h.setAttachmentIcon(lastMessage)
            h.messageText.setTextColor(AppContext.instance.getColor(R.color.font_dark_secondary))
            h.messageTypingImage.visibility = View.GONE

            if (dialog.isChat()) {
                h.messageImage.visibility = View.VISIBLE
                loadImage(h.messageImage, lastMessage.senderOrEmpty().photoUrl)
            }

            if (!dialog.isChat()) {
                h.messageImage.visibility = if (lastMessage.isOut) View.VISIBLE else View.GONE
                if (lastMessage.isOut)
                    loadImage(h.messageImage, lastMessage.senderOrEmpty().photoUrl)
            }
        }

        // Common content
        h.title.text = dialog.getTitle()
        h.messageDate.text = DateFormat.dialogReceivedDate(lastMessage.sentTimeMillis / 1000L)
        h.unreadBackground.visibility = if (lastMessage.isIn && lastMessage.isNotRead) View.VISIBLE else View.INVISIBLE
        h.chooseImageLayoutForImageCount(dialog.getImageCount())
        for (i in 0..dialog.getImageCount() - 1)
            loadImage(h.getImageView(i), dialog.getImageUrl(i))

        // Chat content
        if (dialog.isChat()) {
            h.onlineIcon.visibility = View.GONE
        }

        // Dialog content
        if (!dialog.isChat()) {
            h.onlineIcon.visibility = if (dialog.isOnline()) View.VISIBLE else View.GONE
            h.onlineIcon.setImageResource(when (dialog.deviceType()) {
                Device.MOBILE -> R.drawable.icon_online_mobile
                Device.DESKTOP -> R.drawable.icon_online
            })
        }
    }

    private fun updateList() {
        val dialogs = filtrator.filterSnapshot(snapshot, filters)

        val man = list.layoutManager as LinearLayoutManager
        val curPos = man.findFirstVisibleItemPosition()
        val top = if (curPos != -1)
            man.findViewByPosition(curPos).top
        else
            0

        // Delete items that not present in new collection
        val positionsToRemove = LinkedList<Int>()
        filteredDialogs.forEachIndexed {
            pos, dialog ->
            if (dialogs.none { it isSameDialog dialog })
                positionsToRemove.add(pos)
        }
        positionsToRemove.reversed().forEach {
            filteredDialogs.removeAt(it)
            notifyItemRemoved(it)
        }

        // Add non existing items
        dialogs.forEachIndexed {
            pos, dialog ->
            if (filteredDialogs.none { it isSameDialog dialog }) {
                filteredDialogs.add(pos, dialog)
                notifyItemInserted(pos)
            }
        }

        // Move existing items
        dialogs.forEachIndexed {
            pos, dialog ->
            val oldDialog = filteredDialogs[pos]
            if (oldDialog isNotSameDialog dialog) {
                val index = filteredDialogs.indexOfFirst { it isSameDialog dialog }
                filteredDialogs.set(index, dialog)
                notifyItemChanged(index)
                filteredDialogs.removeAt(index)
                filteredDialogs.add(pos, dialog)
                notifyItemMoved(index, pos)
            } else {
                filteredDialogs.set(pos, dialog)
                if (!(oldDialog isSameDialogAndContent dialog))
                    notifyItemChanged(pos)
            }
        }

        if (curPos != -1)
            man.scrollToPositionWithOffset(curPos, top)
    }

    fun getDialog(pos: Int) = filteredDialogs[pos]

    fun checkForNewDialogs() {
        if (DialogListCache.getSnapshot().snapshotTime > snapshot.snapshotTime) {
            snapshot = DialogListCache.getSnapshot()
            updateList()
        }
    }

    fun checkForFilters() {
        filters = DAOFilters.loadVkFilters()
        updateList()
    }

    private fun loadImage(view: ImageView, url: String) = imageLoader.displayImage(url, view)
}