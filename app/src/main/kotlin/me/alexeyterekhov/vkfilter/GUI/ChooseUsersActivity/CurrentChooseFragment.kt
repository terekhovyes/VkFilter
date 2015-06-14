package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList.CurrentListAdapter
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestChats
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestUsers
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import kotlin.properties.Delegates


public class CurrentChooseFragment(): Fragment(), DataDepend {
    companion object {
        val KEY_SAVED = "CurrentFragmentSaved"
        val KEY_ADAPTER = "CurrentFragmentAdapter"
    }

    var adapter: CurrentListAdapter by Delegates.notNull()

    fun setSelected(
            currentUsers: Set<Long>,
            currentChats: Set<Long>,
            selectedUsers: MutableSet<Long>,
            selectedChats: MutableSet<Long>,
            notifyAction: () -> Unit
    ) {
        adapter = CurrentListAdapter(
                currentUsers,
                currentChats,
                selectedUsers,
                selectedChats,
                notifyAction
        )
        val userIds = currentUsers
                .map { it.toString() }
                .filter { !UserCache.contains(it) }
        val chatIds = currentChats
                .map { it.toString() }
                .filter { !ChatInfoCache.contains(it) }
        if (userIds.isNotEmpty())
            RequestControl addForeground RequestUsers(userIds)
        if (chatIds.isNotEmpty())
            RequestControl addForeground RequestChats(chatIds)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_choose, container, false)

        val recycler = view.findViewById(R.id.recyclerList) as RecyclerView
        recycler.setLayoutManager(LinearLayoutManager(AppContext.instance))
        recycler.setAdapter(adapter)
        return view
    }

    override fun onCreate(saved: Bundle?) {
        super<Fragment>.onCreate(saved)
        if ((DataSaver removeObject KEY_SAVED) != null) {
            adapter = (DataSaver removeObject KEY_ADAPTER) as CurrentListAdapter
        }
        UserCache.listeners add this
        ChatInfoCache.listeners add this
        adapter.notifyDataSetChanged()
    }
    override fun onDestroy() {
        UserCache.listeners remove this
        ChatInfoCache.listeners remove this
        super<Fragment>.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<Fragment>.onSaveInstanceState(outState)
        DataSaver.putObject(KEY_SAVED, true)
        DataSaver.putObject(KEY_ADAPTER, adapter)
    }

    override fun onDataUpdate() {
        adapter.notifyDataSetChanged()
    }
}