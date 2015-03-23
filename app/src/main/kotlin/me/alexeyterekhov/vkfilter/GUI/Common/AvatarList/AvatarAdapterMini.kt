package me.alexeyterekhov.vkfilter.GUI.Common.AvatarList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.R
import java.util.HashSet
import java.util.Vector


class AvatarAdapterMini(val layoutRes: Int):
        RecyclerView.Adapter<AvatarHolder>()
{
    private val imageLoader = ImageLoader.getInstance()
    private val vkIds = Vector<VkIdentifier>()

    private val userIdsForLoading = HashSet<String>()
    // private val chatIdsForLoading = HashSet<String>()

    fun setIds(ids: List<VkIdentifier>) {
        vkIds.clear()
        userIdsForLoading.clear()

        vkIds addAll ids
        notifyDataSetChanged()

        for (id in vkIds)
            when (id.type) {
                VkIdentifier.TYPE_USER -> {
                    val strId = id.id.toString()
                    if (!UserCache.contains(strId))
                        userIdsForLoading add strId
                }
            }
    }

    fun checkForNewAvatars() {
        var added = false
        val it = userIdsForLoading.iterator()
        while (it.hasNext()) {
            val id = it.next()
            if (UserCache contains id) {
                added = true
                it.remove()
            }
        }
        if (added)
            notifyDataSetChanged()
    }

    override fun getItemCount() = vkIds.size()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AvatarHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(layoutRes, parent, false)
        return AvatarHolder(view)
    }
    override fun onBindViewHolder(h: AvatarHolder, position: Int) {
        val vkId = vkIds get position
        when (vkId.type) {
            VkIdentifier.TYPE_USER -> {
                h.singleImage setVisibility View.VISIBLE
                h.doubleLayout setVisibility View.INVISIBLE
                h.tripleLayout setVisibility View.INVISIBLE
                h.quadLayout setVisibility View.INVISIBLE
                if (UserCache contains vkId.id.toString())
                    imageLoader.displayImage(
                            (UserCache getUser vkId.id.toString())!!.photoUrl,
                            h.singleImage
                    )
                else
                    h.singleImage setImageResource R.drawable.user_photo_loading
            }
            VkIdentifier.TYPE_CHAT -> {

            }
        }
    }
}