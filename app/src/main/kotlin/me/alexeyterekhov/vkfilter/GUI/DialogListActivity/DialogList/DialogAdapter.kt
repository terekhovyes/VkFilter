package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import java.util.LinkedList
import java.util.Vector

class DialogAdapter(val list: RecyclerView) :
        RecyclerView.Adapter<DialogHolder>()
{
    private val imageLoader = ImageLoader.getInstance()

    private var snapshot = DialogListCache.emptySnapshot()
    private var filters = DAOFilters.loadVkFilters()
    private var filteredDialogs = Vector<Dialog>()

    fun getDialog(pos: Int) = filteredDialogs get pos
    override fun getItemCount() = filteredDialogs.size()
    override fun onCreateViewHolder(v: ViewGroup, type: Int): DialogHolder {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_dialog_circle, v, false)
        return DialogHolder(view)
    }
    override fun onBindViewHolder(h: DialogHolder, pos: Int) {
        val dialog = filteredDialogs get pos
        fillContentCommon(h, dialog)
        if (dialog.isChat())
            fillContentChat(h, dialog)
        else
            fillContentPersonal(h, dialog)
    }

    fun checkDialogCache() {
        if (DialogListCache.getSnapshot().snapshotTime > snapshot.snapshotTime) {
            snapshot = DialogListCache.getSnapshot()
            updateListWithNewData()
        }
    }
    fun filterDataAgain() {
        filters = DAOFilters.loadVkFilters()
        updateListWithNewData()
    }
    private fun updateListWithNewData() {
        updateWithAnimation(Filtrator.filter(snapshot, filters))
    }

    private fun updateWithAnimation(newData: Vector<Dialog>) {
        val man = list.getLayoutManager() as LinearLayoutManager
        val curPos = man.findFirstVisibleItemPosition()
        val top = if (curPos != -1)
            man.findViewByPosition(curPos).getTop()
        else
            0

        // Delete items that not present in new collection
        val positionsToRemove = LinkedList<Int>()
        filteredDialogs forEachIndexed {
            pos, dialog ->
            if (newData none { it same dialog })
                positionsToRemove add pos
        }
        positionsToRemove.reverse() forEach {
            filteredDialogs remove it
            notifyItemRemoved(it)
        }

        // Add non existing items
        newData forEachIndexed {
            pos, dialog ->
            if (filteredDialogs none { it same dialog }) {
                filteredDialogs.add(pos, dialog)
                notifyItemInserted(pos)
            }
        }

        // Move existing items
        newData forEachIndexed {
            pos, dialog ->
            val oldDialog = filteredDialogs get pos
            if (oldDialog notSame dialog) {
                val index = filteredDialogs indexOfFirst { it same dialog }
                filteredDialogs.set(index, dialog)
                notifyItemChanged(index)
                filteredDialogs remove index
                filteredDialogs.add(pos, dialog)
                notifyItemMoved(index, pos)
            } else {
                filteredDialogs.set(pos, dialog)
                if (!(oldDialog equals dialog))
                    notifyItemChanged(pos)
            }
        }

        if (curPos != -1)
            man.scrollToPositionWithOffset(curPos, top)
    }

    private fun makeVisible(v: View) = v.setVisibility(View.VISIBLE)
    private fun makeInvisible(v: View) = v.setVisibility(View.INVISIBLE)
    private fun loadImage(view: ImageView, url: String) {
        imageLoader.displayImage(url, view)
    }
    private fun fillContentCommon(h: DialogHolder, data: Dialog) {
        val lastMessage = data.lastMessage!!

        h.title setText data.title

        // last message info
        h.messageDate setText DateFormat.dialogReceivedDate(lastMessage.sentTimeMillis / 1000L)
        h.messageText setText lastMessage.text
        if (lastMessage.isOut) {
            makeInvisible(h.unreadBackground)
            if (!lastMessage.isRead)
                makeVisible(h.unreadMessage)
            else
                makeInvisible(h.unreadMessage)
        } else {
            makeInvisible(h.unreadMessage)
            if (!lastMessage.isRead)
                makeVisible(h.unreadBackground)
            else
                makeInvisible(h.unreadBackground)
        }
        h.setAttachmentIcon(lastMessage)

        // picture
        when (data.getImageCount()) {
            1 -> {
                h.singleImage.setVisibility(View.VISIBLE)
                h.doubleLayout.setVisibility(View.INVISIBLE)
                h.tripleLayout.setVisibility(View.INVISIBLE)
                h.quadLayout.setVisibility(View.INVISIBLE)
                loadImage(h.singleImage, data.getImageUrl(0))
            }
            2 -> {
                h.singleImage.setVisibility(View.INVISIBLE)
                h.doubleLayout.setVisibility(View.VISIBLE)
                h.tripleLayout.setVisibility(View.INVISIBLE)
                h.quadLayout.setVisibility(View.INVISIBLE)
                loadImage(h.doubleImage1, data.getImageUrl(0))
                loadImage(h.doubleImage2, data.getImageUrl(1))
            }
            3 -> {
                h.singleImage.setVisibility(View.INVISIBLE)
                h.doubleLayout.setVisibility(View.INVISIBLE)
                h.tripleLayout.setVisibility(View.VISIBLE)
                h.quadLayout.setVisibility(View.INVISIBLE)
                loadImage(h.tripleImage1, data.getImageUrl(0))
                loadImage(h.tripleImage2, data.getImageUrl(1))
                loadImage(h.tripleImage3, data.getImageUrl(2))
            }
            4 -> {
                h.singleImage.setVisibility(View.INVISIBLE)
                h.doubleLayout.setVisibility(View.INVISIBLE)
                h.tripleLayout.setVisibility(View.INVISIBLE)
                h.quadLayout.setVisibility(View.VISIBLE)
                loadImage(h.quadImage1, data.getImageUrl(0))
                loadImage(h.quadImage2, data.getImageUrl(1))
                loadImage(h.quadImage3, data.getImageUrl(2))
                loadImage(h.quadImage4, data.getImageUrl(3))
            }
        }
    }
    private fun fillContentChat(h: DialogHolder, data: Dialog) {
        h.onlineIcon setVisibility View.GONE
        makeVisible(h.messageImage)
        loadImage(h.messageImage, data.lastMessage!!.senderOrEmpty().photoUrl)
    }
    private fun fillContentPersonal(h: DialogHolder, data: Dialog) {
        if (data.showOnlineIcon())
            makeVisible(h.onlineIcon)
        else
            makeInvisible(h.onlineIcon)
        if (data.lastMessage!!.isOut) {
            makeVisible(h.messageImage)
            loadImage(h.messageImage, data.lastMessage!!.senderOrEmpty().photoUrl)
        } else
            h.messageImage.setVisibility(View.GONE)
    }
}