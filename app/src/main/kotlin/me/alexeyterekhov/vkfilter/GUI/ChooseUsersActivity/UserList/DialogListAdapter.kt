package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.R


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

    override fun getItemCount() = snapshot.dialogs.size() + 1
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
    override fun getItemViewType(p: Int) = if (p == snapshot.dialogs.size()) TYPE_FOOTER else TYPE_ITEM
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_ITEM)
            return

        val dialog = snapshot.dialogs get position

        with (h as DialogItemHolder) {
            if (dialog.isChat() && dialog.photoUrl == "") {
                when (dialog.getPartnersCount()) {
                    2 -> {
                        singlePic setVisibility View.GONE
                        doubleLayout setVisibility View.VISIBLE
                        tripleLayout setVisibility View.GONE
                        quadLayout setVisibility View.GONE
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(0), doublePic1)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(1), doublePic2)
                    }
                    3 -> {
                        singlePic setVisibility View.GONE
                        doubleLayout setVisibility View.GONE
                        tripleLayout setVisibility View.VISIBLE
                        quadLayout setVisibility View.GONE
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(0), triplePic1)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(1), triplePic2)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(2), triplePic3)
                    }
                    else -> {
                        singlePic setVisibility View.GONE
                        doubleLayout setVisibility View.GONE
                        tripleLayout setVisibility View.GONE
                        quadLayout setVisibility View.VISIBLE
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(0), quadPic1)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(1), quadPic2)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(2), quadPic3)
                        imageLoader.displayImage(dialog.getPartnerPhotoUrl(3), quadPic4)
                    }
                }
            } else {
                singlePic setVisibility View.VISIBLE
                doubleLayout setVisibility View.GONE
                tripleLayout setVisibility View.GONE
                quadLayout setVisibility View.GONE
                if (dialog.photoUrl == "")
                    imageLoader.displayImage(dialog.getPartnerPhotoUrl(0), singlePic)
                else
                    imageLoader.displayImage(dialog.photoUrl, singlePic)
            }

            name setText dialog.title
            val message = dialog.lastMessage
            if (message != null) {
                imageLoader.displayImage(message.senderOrEmpty().photoUrl, senderIcon)
                lastMessage setText message.text
            }

            with (checkBox) {
                setOnCheckedChangeListener(null)
                setChecked(
                        if (dialog.isChat())
                            selectedChats contains dialog.id
                        else
                            selectedUsers contains dialog.id
                )
                setOnCheckedChangeListener {
                    view, checked ->
                    when {
                        dialog.isChat() && checked -> selectedChats add dialog.id
                        dialog.isChat() && !checked -> selectedChats remove dialog.id
                        !dialog.isChat() && checked -> selectedUsers add dialog.id
                        !dialog.isChat() && !checked -> selectedUsers remove dialog.id
                    }
                    onChangeSelection()
                }
            }
        }
    }
}