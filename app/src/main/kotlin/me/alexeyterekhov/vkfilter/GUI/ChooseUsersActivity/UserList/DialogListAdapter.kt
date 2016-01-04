package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


public class DialogListAdapter(
        val selectedUsers: MutableSet<Long>,
        val selectedChats: MutableSet<Long>,
        val onChangeSelection: () -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var snapshot = DialogListCache.emptySnapshot()
    private val imageLoader = ImageLoader.getInstance()

    val TYPE_ITEM = 1
    val TYPE_FOOTER = 2

    fun checkDialogCache() {
        if (DialogListCache.getSnapshot().snapshotTime != snapshot.snapshotTime) {
            snapshot = DialogListCache.getSnapshot()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = snapshot.dialogs.size + 1
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(AppContext.instance)
        return when (viewType) {
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_dialog_checked, parent, false)
                DialogItemHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_floatbutton_footer, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            }
        }
    }
    override fun getItemViewType(p: Int) = if (p == snapshot.dialogs.size) TYPE_FOOTER else TYPE_ITEM
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_ITEM)
            return

        val dialog = snapshot.dialogs.get(position)

        with (h as DialogItemHolder) {
            if (dialog.isChat() && dialog.chatPhotoUrl == "") {
                when (dialog.partners.count()) {
                    2 -> {
                        singlePic.visibility = View.GONE
                        doubleLayout.visibility = View.VISIBLE
                        tripleLayout.visibility = View.GONE
                        quadLayout.visibility = View.GONE
                        imageLoader.displayImage(dialog.partners[0].photoUrl, doublePic1)
                        imageLoader.displayImage(dialog.partners[1].photoUrl, doublePic2)
                    }
                    3 -> {
                        singlePic.visibility = View.GONE
                        doubleLayout.visibility = View.GONE
                        tripleLayout.visibility = View.VISIBLE
                        quadLayout.visibility = View.GONE
                        imageLoader.displayImage(dialog.partners[0].photoUrl, triplePic1)
                        imageLoader.displayImage(dialog.partners[1].photoUrl, triplePic2)
                        imageLoader.displayImage(dialog.partners[2].photoUrl, triplePic3)
                    }
                    else -> {
                        singlePic.visibility = View.GONE
                        doubleLayout.visibility = View.GONE
                        tripleLayout.visibility = View.GONE
                        quadLayout.visibility = View.VISIBLE
                        imageLoader.displayImage(dialog.partners[0].photoUrl, quadPic1)
                        imageLoader.displayImage(dialog.partners[1].photoUrl, quadPic2)
                        imageLoader.displayImage(dialog.partners[2].photoUrl, quadPic3)
                        imageLoader.displayImage(dialog.partners[3].photoUrl, quadPic4)
                    }
                }
            } else {
                singlePic.visibility = View.VISIBLE
                doubleLayout.visibility = View.GONE
                tripleLayout.visibility = View.GONE
                quadLayout.visibility = View.GONE
                if (dialog.chatPhotoUrl == "")
                    imageLoader.displayImage(dialog.partners[0].photoUrl, singlePic)
                else
                    imageLoader.displayImage(dialog.chatPhotoUrl, singlePic)
            }

            name.text = dialog.getTitle()
            val message = dialog.lastMessage
            if (message != null) {
                imageLoader.displayImage(message.senderOrEmpty().photoUrl, senderIcon)
                lastMessage.text = message.text
            }

            with (checkBox) {
                setOnCheckedChangeListener(null)
                isChecked = if (dialog.isChat())
                    selectedChats.contains(dialog.id)
                else
                selectedUsers.contains(dialog.id)
                setOnCheckedChangeListener {
                    view, checked ->
                    when {
                        dialog.isChat() && checked -> selectedChats.add(dialog.id)
                        dialog.isChat() && !checked -> selectedChats.remove(dialog.id)
                        !dialog.isChat() && checked -> selectedUsers.add(dialog.id)
                        !dialog.isChat() && !checked -> selectedUsers.remove(dialog.id)
                    }
                    onChangeSelection()
                }
            }
        }
    }
}