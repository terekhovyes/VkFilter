package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.FilterGlass

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.Vector
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Common.AppContext
import android.view.LayoutInflater
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapter
import android.support.v7.widget.LinearLayoutManager
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import me.alexeyterekhov.vkfilter.Common.FilterStates
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import com.activeandroid.ActiveAndroid
import android.util.Log


class FilterGlassAdapter(val listener: DataDepend): RecyclerView.Adapter<RecyclerView.ViewHolder>(),
DataDepend {
    class object {
        val TYPE_HEADER = 0
        val TYPE_ITEM = 1
    }

    val filters = Vector<VkFilter>()
    var refreshing = false

    override fun getItemCount() = filters.size() + 1
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
                setAdapter(AvatarAdapter(R.layout.item_avatar_smallest))
                setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, true))
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

        val item = filters.get(position - 1)
        with (holder as FilterHolder) {
            switch setIconRes item.getIconTransparentBackgroundResource()
            switch.setStateWithoutListener(FilterStates.filterToSwitch(item.state), false)
            switch setListener object : TripleSwitchView.OnSwitchChangeStateListener {
                override fun onChangeState(newState: Int) {
                    item.state = FilterStates.switchToFilter(newState)
                    DAOFilters.saveFilter(item)
                    listener.onDataUpdate()
                }
            }
            filterName setText item.filterName
            avatarList.getAdapter() as AvatarAdapter setIds item.identifiers()
        }
    }

    fun resetFilters(recyclerView: RecyclerView) {
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
        val man = recyclerView.getLayoutManager() as LinearLayoutManager
        val from = man.findFirstVisibleItemPosition()
        val to = man.findLastVisibleItemPosition()
        Log.d("debug", "From $from to $to")
        for (i in from..to) {
            val view = man.findViewByPosition(i)
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is FilterHolder)
                holder.switch.setStateWithoutListener(TripleSwitchView.STATE_MIDDLE, true)
        }
        for (i in 0..filters.size() - 1)
            if (i !in from..to)
                notifyItemChanged(i)
        listener.onDataUpdate()
    }

    fun enableRefreshing(enabled: Boolean) {
        refreshing = enabled
    }

    override fun onDataUpdate() {
        if (refreshing)
            notifyDataSetChanged()
    }
}