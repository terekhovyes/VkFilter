package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.content.Intent
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogList.DialogAdapter
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollNew
import me.alexeyterekhov.vkfilter.LibClasses.RecyclerItemClickAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import kotlin.properties.Delegates

class DialogListModule(val activity: DialogsActivity) {
    companion object {
        val LOADING_THRESHOLD = 15
        val LOADING_PORTION = 50
    }

    val clickListener = createClickListener()
    var scrollListener: EndlessScrollNew by Delegates.notNull()
    val cacheListener = createCacheListener()

    fun onCreate() {
        val list = findList()

        if (list.getAdapter() == null) {
            list setAdapter DialogAdapter(list)
            list setLayoutManager LinearLayoutManager(AppContext.instance)
            list setItemAnimator DefaultItemAnimator()
            list.getItemAnimator() setSupportsChangeAnimations true
        }
        list addOnItemTouchListener clickListener
        scrollListener = createEndlessScroll(list)
        list addOnScrollListener scrollListener

        DialogListCache.listeners add cacheListener
        (list.getAdapter() as DialogAdapter).checkForNewDialogs()
    }

    fun onDestroy() {
        DialogListCache.listeners remove cacheListener
        val list = findList()
        list removeOnItemTouchListener clickListener
        list removeOnScrollListener scrollListener
    }

    fun findList() = activity.findViewById(R.id.dialogList) as RecyclerView
    fun getAdapter() = findList().getAdapter() as DialogAdapter?

    private fun createClickListener() = RecyclerItemClickAdapter(AppContext.instance,
            object : RecyclerItemClickAdapter.OnItemClickListener {
                override fun onItemClick(v: View, pos: Int) {
                    val adapter = findList().getAdapter() as DialogAdapter
                    val clickedDialog = adapter.getDialog(pos)

                    val intent = Intent(activity, javaClass<ChatActivity>())
                    val key = if (clickedDialog.isChat()) "chat_id" else "user_id"
                    intent.putExtra(key, clickedDialog.id.toString())
                    intent.putExtra("title", clickedDialog.getTitle())
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.anim.activity_from_right, R.anim.activity_to_left)
                }
            })
    private fun createEndlessScroll(list: RecyclerView) = EndlessScrollNew(
            recyclerView = list,
            activationThreshold = LOADING_THRESHOLD,
            reverse = false,
            onReachThreshold = ({ currentCount ->
                activity.requestModule.loadDialogs(offset = currentCount, count = LOADING_PORTION)
            })
    )
    private fun createCacheListener() = object : DataDepend {
        override fun onDataUpdate() {
            val adapter = findList().getAdapter() as DialogAdapter
            adapter.checkForNewDialogs()
        }
    }
}