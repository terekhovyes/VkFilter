package me.alexeyterekhov.vkfilter.GUI.Common.AvatarList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestChats
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestUsers
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.*


public class AvatarListAdapter(val layoutRes: Int):
        BaseAdapter(),
        DataDepend
{
    private val imageLoader = ImageLoader.getInstance()
    val vkIds = Vector<VkIdentifier>()

    fun setIds(ids: List<VkIdentifier>) {
        vkIds.clear()
        vkIds.addAll(ids)
        notifyDataSetChanged()

        val usersForLoading = vkIds
                .filter { it.type == VkIdentifier.TYPE_USER }
                .map { it.id.toString() }
                .filter { !UserCache.contains(it) }
        val chatsForLoading = vkIds
                .filter { it.type == VkIdentifier.TYPE_CHAT }
                .map { it.id.toString() }
                .filter { !ChatInfoCache.contains(it) }

        if (usersForLoading.isNotEmpty())
            RequestControl addForeground RequestUsers(usersForLoading)
        if (chatsForLoading.isNotEmpty())
            RequestControl addForeground RequestChats(chatsForLoading)
    }

    override fun getCount() = vkIds.size
    override fun getItem(position: Int) = vkIds[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(AppContext.instance)
            val v = inflater.inflate(layoutRes, parent, false)
            v.tag = AvatarHolder(v)
            v
        } else
            convertView

        val h = view.tag as AvatarHolder

        val vkId = vkIds.get(position)
        when (vkId.type) {
            VkIdentifier.TYPE_USER -> {
                setLayoutVisibility(h, 1)
                if (UserCache contains vkId.id.toString())
                    imageLoader.displayImage(
                            (UserCache getUser vkId.id.toString())!!.photoUrl,
                            h.singleImage
                    )
                else
                    h.singleImage.setImageResource(R.drawable.icon_user_stub)
            }
            VkIdentifier.TYPE_CHAT -> {
                if (ChatInfoCache contains vkId.id.toString()) {
                    val chat = (ChatInfoCache getChat vkId.id.toString())!!
                    if (chat.photoUrl != "") {
                        setLayoutVisibility(h, 1)
                        imageLoader.displayImage(chat.photoUrl, h.singleImage)
                    } else {
                        when (chat.chatPartners.size) {
                            0 -> {
                                setLayoutVisibility(h, 1)
                                h.singleImage.setImageResource(R.drawable.icon_user_stub)
                            }
                            1 -> {
                                setLayoutVisibility(h, 1)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.singleImage)
                            }
                            2 -> {
                                setLayoutVisibility(h, 2)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.doubleImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.doubleImage2)
                            }
                            3 -> {
                                setLayoutVisibility(h, 3)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.tripleImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.tripleImage2)
                                imageLoader.displayImage(chat.chatPartners[2].photoUrl, h.tripleImage3)
                            }
                            else -> {
                                setLayoutVisibility(h, 4)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.quadImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.quadImage2)
                                imageLoader.displayImage(chat.chatPartners[2].photoUrl, h.quadImage3)
                                imageLoader.displayImage(chat.chatPartners[3].photoUrl, h.quadImage4)
                            }
                        }
                    }
                } else {
                    setLayoutVisibility(h, 1)
                    h.singleImage.setImageResource(R.drawable.icon_user_stub)
                }
            }
        }

        return view
    }

    override fun onDataUpdate() {
        notifyDataSetChanged()
    }

    private fun setLayoutVisibility(holder: AvatarHolder, picCount: Int) {
        holder.singleImage.visibility = if (picCount == 1) View.VISIBLE else View.INVISIBLE
        holder.doubleLayout.visibility = if (picCount == 2) View.VISIBLE else View.INVISIBLE
        holder.tripleLayout.visibility = if (picCount == 3) View.VISIBLE else View.INVISIBLE
        holder.quadLayout.visibility = if (picCount == 4) View.VISIBLE else View.INVISIBLE
    }
}