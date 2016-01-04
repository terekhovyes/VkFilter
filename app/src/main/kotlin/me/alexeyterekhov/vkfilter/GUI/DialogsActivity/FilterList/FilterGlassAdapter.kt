package me.alexeyterekhov.vkfilter.GUI.DialogsActivity.FilterList

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.activeandroid.ActiveAndroid
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.FilterIcons
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapterMini
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.FilterStates
import java.util.*


class FilterGlassAdapter(
        val list: RecyclerView,
        val filterStateChangeListener: DataDepend
):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        DataDepend
{
    companion object {
        val TYPE_HEADER = 0
        val TYPE_ITEM = 1
    }

    val filters = Vector<VkFilter>()

    override fun getItemCount() = filters.size + 1
    override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        return if (viewType == TYPE_ITEM) {
            val view = inflater.inflate(R.layout.item_filter_switch, parent, false)
            val holder = FilterHolder(view)
            with (holder.avatarList) {
                adapter = AvatarAdapterMini(R.layout.item_avatar_40dp)
                layoutManager = LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, true)
            }
            holder
        } else {
            val view = inflater.inflate(R.layout.item_filter_switch_header, parent, false)
            object: RecyclerView.ViewHolder(view) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_ITEM)
            return

        val item = filters[position - 1]
        with (holder as FilterHolder) {
            switch.setIconRes(FilterIcons.resourceById(item.getIcon()))
            switch.setStateWithoutListener(FilterStates.filterToSwitch(item.state), false)
            switch.setListener({ newState ->
                item.state = FilterStates.switchToFilter(newState)
                DAOFilters.saveFilter(item)
                filterStateChangeListener.onDataUpdate()
            })
            filterName.text = item.filterName
            filterName.visibility = if (item.filterName == "") View.GONE else View.VISIBLE
            avatarList.adapter as AvatarAdapterMini setIds item.identifiers()
        }
    }

    fun resetFilters() {
        ActiveAndroid.beginTransaction()
        try {
            for (f in filters) {
                f.state = VkFilter.STATE_DISABLED
                f.save()
            }
            ActiveAndroid.setTransactionSuccessful()
        } finally {
            ActiveAndroid.endTransaction()
        }
        val man = list.layoutManager as LinearLayoutManager
        val from = man.findFirstVisibleItemPosition()
        val to = man.findLastVisibleItemPosition()
        for (i in from..to) {
            val view = man.findViewByPosition(i)
            val holder = list.getChildViewHolder(view)
            if (holder is FilterHolder)
                holder.switch.setStateWithoutListener(TripleSwitchView.STATE_MIDDLE, true)
        }
        for (i in 0..itemCount - 1)
            if (i !in from..to)
                notifyItemChanged(i)
        filterStateChangeListener.onDataUpdate()
    }

    fun updateVisibleAvatarLists() {
        val man = list.layoutManager as LinearLayoutManager
        val from = man.findFirstVisibleItemPosition()
        val to = man.findLastVisibleItemPosition()
        for (i in from..to) {
            val view = man.findViewByPosition(i)
            val holder = list.getChildViewHolder(view)
            if (holder is FilterHolder)
                (holder.avatarList.adapter as AvatarAdapterMini).checkForNewAvatars()
        }
    }

    override fun onDataUpdate() {
        updateVisibleAvatarLists()
    }
}