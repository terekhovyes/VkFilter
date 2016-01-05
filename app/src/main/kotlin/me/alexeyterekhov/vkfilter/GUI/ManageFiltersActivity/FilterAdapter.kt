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
import java.util.*


class FilterAdapter(f: Vector<VkFilter>): ParallaxRecyclerAdapter<VkFilter>(f) {
    private val selected = HashSet<Long>()

    init {
        implementRecyclerAdapterMethods(object : ParallaxRecyclerAdapter.RecyclerAdapterMethods {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
                val inflater = LayoutInflater.from(AppContext.instance)
                val view = inflater.inflate(R.layout.item_filter, parent, false)
                val holder = FilterItemHolder(view)
                with (holder.avatarList) {
                    adapter = AvatarAdapter(R.layout.item_avatar_filterlist)
                    layoutManager = LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false)
                }
                return holder
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
                val item = data[position]
                with (holder as FilterItemHolder) {
                    icon.setImageResource(FilterIcons.resourceById(item.getIcon()))
                    name.text = item.filterName
                    name.visibility = if (item.filterName == "") View.GONE else View.VISIBLE
                    selection.visibility = if (selected.contains(posToId(position))) View.VISIBLE else View.INVISIBLE
                    avatarList.adapter as AvatarAdapter setIds item.identifiers()
                }
            }
            override fun getItemCount() = data.size
        })
    }

    private fun posToId(pos: Int) = (data[pos]).id
    infix fun select(pos: Int) {
        selected.add(posToId(pos - 1))
        notifyItemChanged(pos)
    }
    fun deselect(pos: Int) {
        selected.remove(posToId(pos - 1))
        notifyItemChanged(pos)
    }
    infix fun selectOrDeselect(pos: Int) {
        if (selected.contains(posToId(pos - 1)))
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

        for (pos in data.size - 1 downTo 0) {
            val f = data[pos]
            val id = f.id
            if (selected.contains(id)) {
                selected.remove(id)
                res.add(f)
                data.removeAt(pos)
                notifyItemRemoved(pos + 1)
            }
        }
        return res
    }
}