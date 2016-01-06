package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.graphics.Point
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList.AttachmentsViewGenerator
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList.ChatAdapter
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import me.alexeyterekhov.vkfilter.Internet.DialogRefresher
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollNew
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver

class MessageListModule(val activity: ChatActivity) {
    val messageListener = createMessageListener()
    val userListener = createUserListener()
    var activityIsResumed = false

    fun onCreate() {
        if (getAdapter() == null) {
            initList()
            initAdapterData()
            scrollToFirstUnread()
            setUpEndlessListener()
            setUpOnLayoutChangeScroller()
        }
        getCache().listeners.add(messageListener)
        UserCache.listeners.add(userListener)
        if (getAdapter()!!.messages.isEmpty())
            activity.requestModule.loadLastMessages()
    }

    fun onResume() {
        activityIsResumed = true
        updateAttachmentGenerator()
        val adapter = getAdapter()!!
        initAdapterData()
        if (adapter.messages.isNotEmpty() && !Mocker.MOCK_MODE) {
            DialogRefresher.start(
                    activity.launchParameters.dialogId(),
                    activity.launchParameters.isChat(),
                    { activity.refreshIndicatorModule.showDelayed() },
                    { activity.refreshIndicatorModule.hide() })
        }
        LongPollControl.start()
    }

    fun onRestoreState() {
        val ids = DataSaver.removeObject("selected_messages") as Collection<Long>?
        if (ids != null) {
            with (getAdapter()!!) {
                selectedMessageIds.clear()
                selectedMessageIds.addAll(ids)
                notifyDataSetChanged()
                onSelectionChangeAction?.invoke()
            }
        }
    }

    fun onSaveState() {
        DataSaver.putObject("selected_messages", getAdapter()!!.getSelectedMessageIds())
    }

    fun onPause() {
        activityIsResumed = false
        DialogRefresher.stop()
        LongPollControl.stop()
    }

    fun onDestroy() {
        getCache().listeners.remove(messageListener)
        UserCache.listeners.remove(userListener)
    }

    fun getList() = (activity.findViewById(R.id.messageList)) as RecyclerView
    fun getAdapter() = getList().adapter as ChatAdapter?
    private fun getCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )
    private fun initList() {
        val list = getList()
        list.adapter = ChatAdapter(
                dialogId = activity.launchParameters.dialogId(),
                isChat = activity.launchParameters.isChat(),
                activity = activity
        )
        list.layoutManager = LinearLayoutManager(AppContext.instance, LinearLayoutManager.VERTICAL, false)
        (list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        updateAttachmentGenerator()
    }
    private fun initAdapterData() {
        getAdapter()?.setData(getCache().getMessages())
    }
    private fun updateAttachmentGenerator() {
        val adapter = getAdapter()
        if (adapter != null) {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            adapter.attachmentGenerator = AttachmentsViewGenerator(
                    activity = activity,
                    maxViewHeight = (size.y * 0.7).toInt(),
                    maxViewWidth = (size.x * 0.7).toInt(),
                    shownUrls = adapter.shownImages
            )
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
                            activity.requestModule.loadMessagesOlderThan(adapter.messages.first().sentId.toString())
                }
        )
        getList().setOnScrollListener(endless)
    }
    private fun setUpOnLayoutChangeScroller() {
        val list = getList()
        list.addOnLayoutChangeListener({ v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (oldBottom != 0) {
                val dif = oldBottom - bottom
                if (dif > 0)
                    list.scrollBy(0, dif)
            }
        })
    }
    private fun isAtBottom(completely: Boolean = false): Boolean {
        val adapter = getAdapter()!!
        val layoutMan = getList().layoutManager as LinearLayoutManager
        return if (!completely) {
            adapter.messages.isEmpty()
                    || layoutMan.findLastVisibleItemPosition() >= adapter.messages.count() - 1
        } else {
            adapter.messages.isEmpty()
                    || layoutMan.findLastCompletelyVisibleItemPosition() >= adapter.messages.count() - 1
        }
    }
    private fun adapterHaveUnreadIncomeMessages() = getAdapter()!!.messages.any { it.isIn && it.isNotRead }
    private fun scrollDown(smooth: Boolean = false) {
        val lastPos = getAdapter()!!.itemCount - 1
        if (smooth)
            getList().smoothScrollToPosition(lastPos)
        else
            getList().scrollToPosition(lastPos)
    }
    private fun scrollToFirstUnread() {
        val adapter = getAdapter()
        if (adapter != null) {
            if (adapter.messages.isNotEmpty()) {
                if (adapter.messages.none { it.isIn && it.isNotRead })
                    scrollDown()
                else {
                    var unreadPos = adapter.messages.indexOfFirst { it.isIn && it.isNotRead }
                    val height = getList().height
                    val offset = Math.max(50, (height * 0.1).toInt())
                    (getList().layoutManager as LinearLayoutManager).scrollToPositionWithOffset(unreadPos, offset)
                }
            }
        }
    }
    private fun createMessageListener() = object : MessageCacheListener {
        override fun onAddNewMessages(messages: Collection<Message>) {
            val adapter = getAdapter()!!
            val atBottom = isAtBottom()
            val haveUnreadMessages = adapterHaveUnreadIncomeMessages()
            adapter.onAddNewMessages(messages)
            if (atBottom) {
                if (haveUnreadMessages)
                    scrollDown()
                else
                    scrollToFirstUnread()
            }
            if (adapter.messages.isNotEmpty() && activityIsResumed && !DialogRefresher.isRunning())
                DialogRefresher.start(
                        activity.launchParameters.dialogId(),
                        activity.launchParameters.isChat(),
                        { activity.refreshIndicatorModule.showDelayed() },
                        { activity.refreshIndicatorModule.hide() })
        }
        override fun onAddOldMessages(messages: Collection<Message>) {
            getAdapter()?.onAddOldMessages(messages)
        }
        override fun onReplaceMessage(old: Message, new: Message) {
            val atBottom = isAtBottom()
            getAdapter()?.onReplaceMessage(old, new)
            if (atBottom)
                scrollDown()
        }
        override fun onUpdateMessages(messages: Collection<Message>) {
            val atBottom = isAtBottom()
            getAdapter()?.onUpdateMessages(messages)
            if (atBottom)
                Handler().postDelayed({ scrollDown(smooth = true) }, 500)
        }
        override fun onReadMessages(messages: Collection<Message>) {
            getAdapter()?.onReadMessages(messages)
        }
    }
    private fun createUserListener() = object : DataDepend {
        override fun onDataUpdate() {
            getAdapter()?.notifyDataSetChanged()
        }
    }
}