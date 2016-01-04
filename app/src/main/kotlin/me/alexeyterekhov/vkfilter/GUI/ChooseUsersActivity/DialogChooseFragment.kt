package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList.DialogListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestDialogList
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollListener
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import kotlin.properties.Delegates


public class DialogChooseFragment: Fragment(), DataDepend {
    companion object {
        val KEY_SAVED = "DialogFragmentSaved"
        val KEY_ADAPTER = "DialogFragmentAdapter"
    }

    var adapter: DialogListAdapter by Delegates.notNull()
    val LOAD_PORTION = 50
    val LOAD_THRESHOLD = 15

    fun setSelected(
            users: MutableSet<Long>,
            chats: MutableSet<Long>,
            notifyAction: () -> Unit
    ) {
        adapter = DialogListAdapter(users, chats, notifyAction)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_choose, container, false)

        val recycler = view.findViewById(R.id.recyclerList) as RecyclerView
        recycler.layoutManager = LinearLayoutManager(AppContext.instance)
        recycler.adapter = adapter

        val endless = object: EndlessScrollListener(recycler, LOAD_THRESHOLD) {
            override fun onReachThreshold(currentItemCount: Int) {
                RequestControl addForeground RequestDialogList(
                        offset = DialogListCache.getSnapshot().dialogs.size,
                        count = LOAD_PORTION
                )
            }
        }
        recycler.setOnScrollListener(endless)

        val refreshLayout = view.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout
        refreshLayout.setRecyclerView(recycler)
        refreshLayout.setOnRefreshListener {
            RequestControl addForeground RequestDialogList(offset = 0, count = LOAD_PORTION)
        }
        refreshLayout.setColorSchemeResources(
                R.color.ui_refresh1,
                R.color.ui_refresh2,
                R.color.ui_refresh3,
                R.color.ui_refresh4
        )

        refreshLayout.isRefreshing = true
        RequestControl addForeground RequestDialogList(offset = 0, count = LOAD_PORTION)

        return view
    }

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        if ((DataSaver removeObject KEY_SAVED) != null) {
            adapter = (DataSaver removeObject KEY_ADAPTER) as DialogListAdapter
        }
        DialogListCache.listeners.add(this)
        adapter.checkDialogCache()
    }
    override fun onDestroy() {
        DialogListCache.listeners.remove(this)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        DataSaver.putObject(KEY_SAVED, true)
        DataSaver.putObject(KEY_ADAPTER, adapter)
    }

    override fun onDataUpdate() {
        (view.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout).isRefreshing = false
        adapter.checkDialogCache()
    }
}