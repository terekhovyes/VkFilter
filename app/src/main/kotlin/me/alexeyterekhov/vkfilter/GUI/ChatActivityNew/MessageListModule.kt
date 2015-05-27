package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.MessageNew
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.MessageList.AttachmentsViewGenerator
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.MessageList.ChatAdapter
import me.alexeyterekhov.vkfilter.Internet.DialogRefresher
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollNew
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext

class MessageListModule(val activity: ChatActivity) {
    val messageListener = createMessageListener()
    val userListener = createUserListener()
    var activityIsResumed = false

    fun onCreate() {
        if (getAdapter() == null) {
            initList()
            initAdapterData()
            setPositionToLastUnread()
            setUpEndlessListener()
        }
        getCache().listeners add messageListener
        UserCache.listeners add userListener
        if (getAdapter()!!.messages.isEmpty())
            activity.requestModule.loadLastMessages()
    }

    fun onResume() {
        activityIsResumed = true
        updateAttachmentGenerator()
        val adapter = getAdapter()!!
        initAdapterData()
        if (adapter.messages.isNotEmpty())
            DialogRefresher.start(activity.launchParameters.dialogId(), activity.launchParameters.isChat())
    }

    fun onPause() {
        activityIsResumed = false
        DialogRefresher.stop()
    }

    fun onDestroy() {
        getCache().listeners remove messageListener
        UserCache.listeners remove userListener
    }

    private fun getList() = (activity findViewById R.id.messageList) as RecyclerView
    private fun getAdapter() = getList().getAdapter() as ChatAdapter?
    private fun getCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )
    private fun initList() {
        val list = getList()
        list.setAdapter(ChatAdapter(
                dialogId = activity.launchParameters.dialogId(),
                isChat = activity.launchParameters.isChat(),
                activity = activity
        ))
        list setLayoutManager LinearLayoutManager(AppContext.instance, LinearLayoutManager.VERTICAL, false)
        list.getItemAnimator().setSupportsChangeAnimations(true)
        updateAttachmentGenerator()
    }
    private fun initAdapterData() {
        val adapter = getAdapter()
        if (adapter != null) {
            adapter.messages.clear()
            adapter.messages addAll getCache().getMessages()
            adapter.notifyDataSetChanged()
        }
    }
    private fun updateAttachmentGenerator() {
        val adapter = getAdapter()
        if (adapter != null) {
            val size = Point()
            activity.getWindowManager().getDefaultDisplay().getSize(size)
            adapter.attachmentGenerator = AttachmentsViewGenerator(
                    activity = activity,
                    maxViewHeight = (size.y * 0.7).toInt(),
                    maxViewWidth = (size.x * 0.7).toInt(),
                    shownUrls = adapter.shownImages
            )
        }
    }
    private fun setPositionToLastUnread() {
        val adapter = getAdapter()
        if (adapter != null) {
            if (adapter.messages.isNotEmpty()) {
                if (adapter.messages none { it.isIn && it.isNotRead })
                    getList() scrollToPosition adapter.messages.count() - 1
                else {
                    var unreadPos = adapter.messages.indexOfFirst { it.isIn && it.isNotRead }
                    if (unreadPos > 0)
                        unreadPos -= 1
                    getList() scrollToPosition unreadPos
                }
            }
        }
    }
    private fun setUpEndlessListener() {
        val endless = EndlessScrollNew(
                recyclerView = getList(),
                activationThreshold = RequestModule.LOAD_THRESHOLD,
                reverse = true,
                onReachThreshold = { currentCount ->
                    val adapter = getAdapter()
                    if (adapter != null)
                        if (adapter.messages.isEmpty())
                            activity.requestModule.loadLastMessages()
                        else
                            activity.requestModule.loadMessagesOlderThan(adapter.messages.first()!!.sentId.toString())
                }
        )
        getList().setOnScrollListener(endless)
    }
    private fun isAtBottom(): Boolean {
        val adapter = getAdapter()!!
        val layoutMan = getList().getLayoutManager() as LinearLayoutManager
        return adapter.messages.isEmpty()
                || layoutMan.findLastVisibleItemPosition() == adapter.messages.count() - 1
    }
    private fun scrollDown() = getList().scrollToPosition(getAdapter()!!.messages.count() - 1)
    private fun createMessageListener() = object : MessageCacheListener {
        override fun onAddNewMessages(count: Int) {
            val adapter = getAdapter()!!
            val atBottom = isAtBottom()
            adapter.onAddNewMessages(count)
            if (atBottom)
                scrollDown()

            if (adapter.messages.isNotEmpty() && activityIsResumed && !DialogRefresher.isRunning())
                DialogRefresher.start(activity.launchParameters.dialogId(), activity.launchParameters.isChat())
        }
        override fun onAddOldMessages(count: Int) {
            getAdapter()?.onAddOldMessages(count)
        }
        override fun onReplaceMessage(old: MessageNew, new: MessageNew) {
            val atBottom = isAtBottom()
            getAdapter()?.onReplaceMessage(old, new)
            if (atBottom)
                scrollDown()
        }
        override fun onUpdateMessages(messages: Collection<MessageNew>) {
            val atBottom = isAtBottom()
            getAdapter()?.onUpdateMessages(messages)
            if (atBottom)
                scrollDown()
        }
        override fun onReadMessages(messages: Collection<MessageNew>) {
            getAdapter()?.onReadMessages(messages)
        }
    }
    private fun createUserListener() = object : DataDepend {
        override fun onDataUpdate() {
            getAdapter()?.notifyDataSetChanged()
        }
    }
}