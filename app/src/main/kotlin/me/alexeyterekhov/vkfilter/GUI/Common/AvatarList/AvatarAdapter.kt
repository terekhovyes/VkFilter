package me.alexeyterekhov.vkfilter.GUI.Common.AvatarList

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import android.view.LayoutInflater
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.R
import android.view.View
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.Vector
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend


class AvatarAdapter(val layoutRes: Int): RecyclerView.Adapter<AvatarHolder>(), DataDepend {
    private val imageLoader = ImageLoader.getInstance()
    val vkIds = Vector<VkIdentifier>()

    fun setIds(ids: List<VkIdentifier>) {
        vkIds.clear()
        vkIds addAll ids
        notifyDataSetChanged()
        val usersForLoading = Vector<String>()
        for (id in vkIds)
            if (id.type == VkIdentifier.TYPE_USER)
                if (!UserCache.contains(id.id.toString()))
                    usersForLoading add id.id.toString()
        if (usersForLoading.isNotEmpty())
            RunFun.userInfo(usersForLoading)
    }

    override fun onDataUpdate() {
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