package me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.Vector
import me.alexeyterekhov.vkfilter.Database.VkFilter
import android.view.LayoutInflater
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.R
import java.util.HashSet
import android.view.View
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapter
import android.support.v7.widget.LinearLayoutManager
import com.poliveira.parallaxrecycleradapter.ParallaxRecyclerAdapter


class FilterAdapter2(f: Vector<VkFilter>): ParallaxRecyclerAdapter<VkFilter>(f) {
    private val selected = HashSet<Long>();

    {
        implementRecyclerAdapterMethods(object : ParallaxRecyclerAdapter.RecyclerAdapterMethods {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
                val inflater = LayoutInflater.from(AppContext.instance)
                val view = inflater.inflate(R.layout.item_filter, parent, false)
                val holder = FilterItemHolder(view)
                with (holder.avatarList) {
                    setAdapter(AvatarAdapter(R.layout.item_avatar_small))
                    setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
                }
                return holder
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
                val item = getData().get(position)
                with (holder as FilterItemHolder) {
                    icon setImageResource item.getIconResource()
                    name setText item.filterName
                    selection setVisibility if (selected contains posToId(position)) View.VISIBLE else View.INVISIBLE
                    avatarList.getAdapter() as AvatarAdapter setIds item.identifiers()
                }
            }
            override fun getItemCount() = getData().size()
        })
    }

    private fun posToId(pos: Int) = (getData() get pos).getId()
    fun select(pos: Int) {
        selected add posToId(pos)
        notifyItemChanged(pos)
    }
    fun deselect(pos: Int) {
        selected remove posToId(pos)
        notifyItemChanged(pos)
    }
    fun selectOrDeselect(pos: Int) {
        if (selected contains posToId(pos))
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
                notifyItemRemoved(pos)
            }
        }
        return res
    }
}

class FilterAdapter(): RecyclerView.Adapter<FilterItemHolder>() {
    val filters = Vector<VkFilter>()
    private val selected = HashSet<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItemHolder {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_filter, parent, false)
        val holder = FilterItemHolder(view)
        with (holder.avatarList) {
            setAdapter(AvatarAdapter(R.layout.item_avatar_small))
            setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
        }
        return holder
    }
    override fun onBindViewHolder(holder: FilterItemHolder, position: Int) {
        val item = filters.get(position)
        with (holder) {
            icon setImageResource item.getIconResource()
            name setText item.filterName
            selection setVisibility if (selected contains posToId(position)) View.VISIBLE else View.INVISIBLE
            avatarList.getAdapter() as AvatarAdapter setIds item.identifiers()
        }
    }
    override fun getItemCount() = filters.size()

    private fun posToId(pos: Int) = (filters get pos).getId()
    fun select(pos: Int) {
        selected add posToId(pos)
        notifyItemChanged(pos)
    }
    fun deselect(pos: Int) {
        selected remove posToId(pos)
        notifyItemChanged(pos)
    }
    fun selectOrDeselect(pos: Int) {
        if (selected contains posToId(pos))
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

        for (pos in filters.size() - 1 downTo 0) {
            val f = filters get pos
            val id = f.getId()
            if (selected contains id) {
                selected remove id
                res add f
                filters remove pos
                notifyItemRemoved(pos)
            }
        }
        return res
    }
}