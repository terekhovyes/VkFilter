package me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.poliveira.parallaxrecycleradapter.ParallaxRecyclerAdapter
import me.alexeyterekhov.vkfilter.Database.FilterIcons
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.HashSet
import java.util.Vector


class FilterAdapter(f: Vector<VkFilter>): ParallaxRecyclerAdapter<VkFilter>(f) {
    private val selected = HashSet<Long>()

    init {
        implementRecyclerAdapterMethods(object : ParallaxRecyclerAdapter.RecyclerAdapterMethods {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
                val inflater = LayoutInflater.from(AppContext.instance)
                val view = inflater.inflate(R.layout.item_filter, parent, false)
                val holder = FilterItemHolder(view)
                with (holder.avatarList) {
                    setAdapter(AvatarAdapter(R.layout.item_avatar_35dp))
                    setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
                }
                return holder
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
                val item = getData().get(position)
                with (holder as FilterItemHolder) {
                    icon setImageResource FilterIcons.resourceById(item.getIcon())
                    name setText item.filterName
                    name setVisibility if (item.filterName == "") View.GONE else View.VISIBLE
                    selection setVisibility if (selected contains posToId(position)) View.VISIBLE else View.INVISIBLE
                    avatarList.getAdapter() as AvatarAdapter setIds item.identifiers()
                }
            }
            override fun getItemCount() = getData().size()
        })
    }

    private fun posToId(pos: Int) = (getData() get pos).getId()
    fun select(pos: Int) {
        selected add posToId(pos - 1)
        notifyItemChanged(pos)
    }
    fun deselect(pos: Int) {
        selected remove posToId(pos - 1)
        notifyItemChanged(pos)
    }
    fun selectOrDeselect(pos: Int) {
        if (selected contains posToId(pos - 1))
            deselect(pos)
        else
            select(pos)
    }
    fun deselectAll() {
        selected.clear()
        notifyDataSetChanged()
    }
    fun nothingSelected() = selected.isEmpty()
    fun removeSelected(): Vector<VkFilter> {
        val res = Vector<VkFilter>()

        for (pos in getData().size() - 1 downTo 0) {
            val f = getData() get pos
            val id = f.getId()
            if (selected contains id) {
                selected remove id
                res add f
                getData() remove pos
                notifyItemRemoved(pos + 1)
            }
        }
        return res
    }
}