package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.R


public class CurrentListAdapter(
        val currentUsers: Set<Long>,
        val currentChats: Set<Long>,
        val selectedUsers: MutableSet<Long>,
        val selectedChats: MutableSet<Long>,
        val onChangeSelection: () -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val imageLoader = ImageLoader.getInstance()

    val TYPE_ITEM = 1
    val TYPE_FOOTER = 2

    override fun getItemCount() = currentUsers.size() + currentChats.size() + 1
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(AppContext.instance)
        return when (viewType) {
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_user_checked, parent, false)
                UserItemHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_floatbutton_footer, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            }
        }
    }
    override fun getItemViewType(p: Int) = if (p == getItemCount() - 1) TYPE_FOOTER else TYPE_ITEM
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_ITEM)
            return

        with (h as UserItemHolder) {
            checkBox setOnCheckedChangeListener null

            var setPos = position
            if (setPos < currentUsers.size()) {
                val userId = currentUsers.toArrayList()[setPos]

                singlePic setVisibility View.VISIBLE
                doubleLayout setVisibility View.GONE
                tripleLayout setVisibility View.GONE
                quadLayout setVisibility View.GONE
                if (UserCache contains userId.toString()) {
                    val user = (UserCache getUser userId.toString())!!
                    if (user.photoUrl != "")
                        imageLoader.displayImage(user.photoUrl, singlePic)
                    else
                        singlePic setImageResource R.drawable.stub_user
                    name setText TextFormat.userTitle(user, false)
                } else {
                    singlePic setImageResource R.drawable.stub_user
                    name setText ""
                }

                checkBox setChecked (selectedUsers contains userId)
                checkBox.setOnCheckedChangeListener {
                    view, checked ->
                    when {
                        checked -> selectedUsers add userId
                        else -> selectedUsers remove userId
                    }
                    onChangeSelection()
                }
            } else {
                setPos -= currentUsers.size()
                val chatId = currentChats.toArrayList()[setPos]

                if (ChatInfoCache contains chatId.toString()) {
                    val chat = (ChatInfoCache getChat chatId.toString())!!
                    if (chat.photoUrl != "") {
                        singlePic setVisibility View.VISIBLE
                        doubleLayout setVisibility View.GONE
                        tripleLayout setVisibility View.GONE
                        quadLayout setVisibility View.GONE
                        imageLoader.displayImage(chat.photoUrl, singlePic)
                    } else {
                        when (chat.chatPartners.size()) {
                            2 -> {
                                singlePic setVisibility View.GONE
                                doubleLayout setVisibility View.VISIBLE
                                tripleLayout setVisibility View.GONE
                                quadLayout setVisibility View.GONE
                                imageLoader.displayImage(chat.getImageUrl(0), doublePic1)
                                imageLoader.displayImage(chat.getImageUrl(1), doublePic2)
                            }
                            3 -> {
                                singlePic setVisibility View.GONE
                                doubleLayout setVisibility View.GONE
                                tripleLayout setVisibility View.VISIBLE
                                quadLayout setVisibility View.GONE
                                imageLoader.displayImage(chat.getImageUrl(0), triplePic1)
                                imageLoader.displayImage(chat.getImageUrl(1), triplePic2)
                                imageLoader.displayImage(chat.getImageUrl(2), triplePic3)
                            }
                            else -> {
                                singlePic setVisibility View.GONE
                                doubleLayout setVisibility View.GONE
                                tripleLayout setVisibility View.GONE
                                quadLayout setVisibility View.VISIBLE
                                imageLoader.displayImage(chat.getImageUrl(0), quadPic1)
                                imageLoader.displayImage(chat.getImageUrl(1), quadPic2)
                                imageLoader.displayImage(chat.getImageUrl(2), quadPic3)
                                imageLoader.displayImage(chat.getImageUrl(3), quadPic4)
                            }
                        }
                    }
                    name setText chat.title
                } else {
                    singlePic setImageResource R.drawable.stub_user
                    name setText ""
                }
                checkBox setChecked (selectedChats contains chatId)
                checkBox.setOnCheckedChangeListener {
                    view, checked ->
                    when {
                        checked -> selectedChats add chatId
                        else -> selectedChats remove chatId
                    }
                    onChangeSelection()
                }
            }
        }
    }
}