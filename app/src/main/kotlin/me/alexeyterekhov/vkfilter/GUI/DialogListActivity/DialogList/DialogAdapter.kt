package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList

import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import android.view.ViewGroup
import android.view.LayoutInflater
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.R
import android.view.View
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Database.DAOFilters
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
        filteredDialogs = Filtrator.filter(snapshot, filters)
        notifyDataSetChanged()
    }
    /*fun checkDialogCache() {
        Log.d("debug", "check dialog cache on $this")
        if (DialogListCache.getSnapshot().snapshotTime > snapshot.snapshotTime) {
            val newSnapshot = DialogListCache.getSnapshot()
            val updatedCount = ChangeAnalyzer.countUpdatedDialogs(snapshot, newSnapshot)
            if (updatedCount == newSnapshot.dialogs.size()) {
                notifyDataSetChanged()
            } else {
                val curPos = (list.getLayoutManager() as LinearLayoutManager).findFirstVisibleItemPosition()
                for (i in 0..updatedCount - 1) {
                    val oldPos = ChangeAnalyzer.findOldPositionOfDialog(snapshot, newSnapshot, i)
                    if (oldPos == -1) {
                        Log.d("debug", "insert $i")
                        notifyItemInserted(i)
                    } else {
                        Log.d("debug", "move $oldPos to $i")
                        notifyItemMoved(oldPos, i)
                        notifyItemChanged(i)
                    }
                }
                if (curPos == 0)
                    list.scrollToPosition(0)
            }
            snapshot = newSnapshot
        }
    }*/
    /*fun checkDialogCache() {
        Log.d("debug", "check dialog cache on $this")
        if (DialogListCache.getSnapshot().snapshotTime > snapshot.snapshotTime) {
            val newSnapshot = DialogListCache.getSnapshot()
            val oldSnapshot = snapshot
            snapshot = newSnapshot

            // Global changes, instead of "notifyDataSetChanged()"
            if (newSnapshot.dialogs.size() < oldSnapshot.dialogs.size())
                notifyItemRangeRemoved(newSnapshot.dialogs.size(), oldSnapshot.dialogs.size())
            if (newSnapshot.dialogs.size() > oldSnapshot.dialogs.size())
                notifyItemRangeInserted(oldSnapshot.dialogs.size(), newSnapshot.dialogs.size())

            // Animate visible range
            if (oldSnapshot.dialogs.isNotEmpty()) {
                // Visible range
                val firstVisible = (list.getLayoutManager() as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastVisible = (list.getLayoutManager() as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastVisible < newSnapshot.dialogs.size()) {
                    val handledItems = HashSet<Long>()
                    for (i in firstVisible..lastVisible) {
                        val d = oldSnapshot.dialogs[i]
                        handledItems add d.id
                        val newPos = ChangeAnalyzer.findPositionOfDialog(newSnapshot, d)
                        if (newPos == -1)
                            notifyItemRemoved(i)
                        else {
                            notifyItemMoved(i, newPos)
                            notifyItemChanged(newPos)
                        }
                    }
                    for (i in firstVisible..lastVisible) {
                        val d = newSnapshot.dialogs[i]
                        if (handledItems contains d.id)
                            continue
                        val oldPos = ChangeAnalyzer.findPositionOfDialog(oldSnapshot, d)
                        if (oldPos == -1)
                            notifyItemInserted(i)
                        else {
                            notifyItemMoved(oldPos, i)
                            notifyItemChanged(i)
                        }
                    }
                    list.scrollToPosition(firstVisible)
                }
            }
        }
    }*/

    private fun makeVisible(v: View) = v.setVisibility(View.VISIBLE)
    private fun makeInvisible(v: View) = v.setVisibility(View.INVISIBLE)
    private fun loadImage(view: ImageView, url: String) {
        imageLoader.displayImage(url, view)
    }
    private fun fillContentCommon(h: DialogHolder, data: Dialog) {
        val lastMessage = data.lastMessage!!

        h.title setText data.title

        // last message info
        h.messageDate setText lastMessage.formattedDate
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

        // picture
        when (data.getImageCount()) {
            1 -> {
                h.singleImage.setVisibility(View.VISIBLE)
                h.doubleLayout.setVisibility(View.GONE)
                h.tripleLayout.setVisibility(View.GONE)
                h.quadLayout.setVisibility(View.GONE)
                loadImage(h.singleImage, data.getImageUrl(0))
            }
            2 -> {
                h.singleImage.setVisibility(View.GONE)
                h.doubleLayout.setVisibility(View.VISIBLE)
                h.tripleLayout.setVisibility(View.GONE)
                h.quadLayout.setVisibility(View.GONE)
                loadImage(h.doubleImage1, data.getImageUrl(0))
                loadImage(h.doubleImage2, data.getImageUrl(1))
            }
            3 -> {
                h.singleImage.setVisibility(View.GONE)
                h.doubleLayout.setVisibility(View.GONE)
                h.tripleLayout.setVisibility(View.VISIBLE)
                h.quadLayout.setVisibility(View.GONE)
                loadImage(h.tripleImage1, data.getImageUrl(0))
                loadImage(h.tripleImage2, data.getImageUrl(1))
                loadImage(h.tripleImage3, data.getImageUrl(2))
            }
            4 -> {
                h.singleImage.setVisibility(View.GONE)
                h.doubleLayout.setVisibility(View.GONE)
                h.tripleLayout.setVisibility(View.GONE)
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
        loadImage(h.messageImage, data.lastMessage!!.sender.photoUrl)
    }
    private fun fillContentPersonal(h: DialogHolder, data: Dialog) {
        if (data.showOnlineIcon())
            makeVisible(h.onlineIcon)
        else
            makeInvisible(h.onlineIcon)
        if (data.lastMessage!!.isOut) {
            makeVisible(h.messageImage)
            loadImage(h.messageImage, data.lastMessage!!.sender.photoUrl)
        } else
            h.messageImage.setVisibility(View.GONE)
    }
}