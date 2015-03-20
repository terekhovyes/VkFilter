package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import com.vk.sdk.VKUIHelper
import me.alexeyterekhov.vkfilter.Common.DataSaver
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.MessageCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Sex
import me.alexeyterekhov.vkfilter.Internet.DialogRefresher
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.R

class ChatActivity: ActionBarActivity(), DataDepend {
    companion object {
        val KEY_SAVED = "ChatActivitySaved"
        val KEY_ADAPTER = "ChatActivityAdapter"
        val KEY_INDEX = "ChatActivityIndex"
        val KEY_TOP = "ChatActivityTop"
    }

    val MESSAGE_PORTION = 40
    private val DIALOG_LOAD_VALUE = 20
    private var id = ""
    private var isChat = false
    private var title = ""
    private var adapter: MessageListAdapter? = null
    private var loadingMessages: Boolean = false
    private var allMessagesGot = false
    private var adapterIsEmpty = true

    private val messageCacheListener = createMessageListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        VKUIHelper.onCreate(this)
        parseIntent()
        initActionBar()
        setContentView(R.layout.activity_chat)
        tryRestoreAdapter()
        initUI()
        MessageCache.getDialog(id, isChat).listeners add messageCacheListener
        MessageCache.getDialog(id, isChat).listeners add this
    }

    override fun onStart() {
        super<ActionBarActivity>.onStart()
    }

    override fun onResume() {
        super<ActionBarActivity>.onResume()
        refreshAdapterImageSize()
        VKUIHelper.onResume(this)
        tryRestoreAdapter()
        VkRequestControl.resume()
        DialogRefresher.start(id, isChat)
        if (!isChat) refreshUserLastSeen()
    }

    override fun onPause() {
        super<ActionBarActivity>.onPause()
        DialogRefresher.stop()
        VkRequestControl.pause()
    }

    override fun onStop() {
        super<ActionBarActivity>.onStop()
    }

    override fun onDestroy() {
        super<ActionBarActivity>.onDestroy()
        VKUIHelper.onDestroy(this)
        MessageCache.getDialog(id, isChat).listeners remove messageCacheListener
        MessageCache.getDialog(id, isChat).listeners remove this
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super<ActionBarActivity>.onSaveInstanceState(outState)

        val list = findViewById(R.id.messageList) as ListView
        val index = list.getFirstVisiblePosition()
        val view = list.getChildAt(0)
        val viewTop = if (view == null) 0 else view.getTop()

        with (DataSaver) {
            putObject(KEY_SAVED, true)
            putObject(KEY_ADAPTER, adapter)
            putObject(KEY_INDEX, index)
            putObject(KEY_TOP, viewTop)
        }
    }

    override fun onBackPressed() {
        super<ActionBarActivity>.onBackPressed()
        overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super<ActionBarActivity>.onOptionsItemSelected(item)
        }
    }

    fun tryRestoreAdapter() {
        if ((DataSaver removeObject KEY_SAVED) != null) {
            val list = findViewById(R.id.messageList) as ListView
            adapter = (DataSaver removeObject KEY_ADAPTER) as MessageListAdapter
            if (adapter != null)
                list setAdapter adapter!!
            val index = (DataSaver removeObject KEY_INDEX) as Int
            val top = (DataSaver removeObject KEY_TOP) as Int
            list.setSelectionFromTop(index, top)
        }
    }

    fun initUI() {
        val listView = findViewById(R.id.messageList) as ListView
        val messageText = findViewById(R.id.messageText) as EditText
        val sendButton = findViewById(R.id.sendButton) as ImageView
        if (adapter == null) {
            val size = Point()
            getWindowManager().getDefaultDisplay().getSize(size)

            adapter = MessageListAdapter(
                    activity = this,
                    id = id,
                    chat = isChat
            )
            adapter!!.maxImageHeight = size.y * 2 / 3
            adapter!!.maxImageWidth = size.x * 2 / 3
            listView.setAdapter(adapter!!)
        }
        listView.setOnScrollListener(createScrollListener())
        listView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (oldBottom != 0 && oldBottom > bottom) {
                    val dif = oldBottom - bottom
                    listView.scrollListBy(dif)
                }
            }
        })
        sendButton.setOnClickListener {
            val text = messageText.getText()?.toString()
            if (text != null && text != "") {
                val outMessage = MessageForSending()
                outMessage.dialogId = id
                outMessage.isChat = isChat
                outMessage.text = text
                RunFun.sendMessage(outMessage)
                messageText.setText("")
            }
        }
    }

    fun parseIntent() {
        val intent = getIntent()
        if (intent != null) {
            val userId = intent.getStringExtra("user_id")
            val chatId = intent.getStringExtra("chat_id")
            if (userId == null && chatId == null)
                return
            id = if (userId == null) chatId!! else userId
            isChat = userId == null
            title = intent.getStringExtra("title")!!
        }
    }

    fun initActionBar() {
        setTitle(title)
        getActionBar()?.setDisplayHomeAsUpEnabled(true)
    }

    fun createScrollListener() = object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (firstVisibleItem < DIALOG_LOAD_VALUE) {
                if (adapter != null && adapter!!.getCount() > 0)
                    loadMoreMessages(adapter?.getItem(0)!!.id.toString())
                else
                    loadMoreMessages()
            }
        }
    }

    fun createMessageListener() = object : DataDepend {
        override fun onDataUpdate() {
            loadingMessages = false
            if (adapterIsEmpty) {
                adapterIsEmpty = false
                adapter!!.notifyOnNewMessages(findViewById(R.id.messageList) as ListView)
            } else {
                with (MessageCache.getDialog(id, isChat)) {
                    if (info.addedMessagesCount > 0) {
                        allMessagesGot = allHistoryLoaded
                        adapter!!.notifyOnNewMessages(findViewById(R.id.messageList) as ListView)
                    }
                    if (info.markedFrom > 0 && info.markedTo > 0) {
                        Log.d("debug", "MARKED IN ADAPTER")
                        adapter!!.markAsRead(
                                findViewById(R.id.messageList) as ListView,
                                info.markedFrom, info.markedTo, info.markedMessagesAreIncomes)
                    }
                }
            }
        }
    }

    fun loadMoreMessages(lastMessageId: String = "", count: Int = MESSAGE_PORTION, offset: Int = 0) {
        if (!loadingMessages && !allMessagesGot) {
            loadingMessages = true
            RunFun.messageList(
                    dialogId = id,
                    dialogIsChat = isChat,
                    offset = offset,
                    count = count,
                    startMessageId = lastMessageId
            )
        }
    }

    fun refreshAdapterImageSize() {
        val size = Point()
        getWindowManager().getDefaultDisplay().getSize(size)
        adapter!!.maxImageHeight = size.y * 3 / 4
        adapter!!.maxImageWidth = size.x * 3 / 4
    }

    fun refreshUserLastSeen() {
        if (!isChat && UserCache.contains(id)) {
            val u = UserCache.getUser(id)!!
            getSupportActionBar().setSubtitle(when {
                u.isOnline -> "Онлайн"
                u.lastOnlineTime == 0L -> ""
                else -> {
                    val t = u.lastOnlineTime
                    val lastVisit = when (u.sex) {
                        Sex.WOMAN -> getString(R.string.last_visit_woman)
                        Sex.MAN -> getString(R.string.last_visit_man)
                        else -> getString(R.string.last_visit_common)
                    }
                    "$lastVisit ${DateFormat.lastOnline(t)}"
                }
            })
        }
    }

    override fun onDataUpdate() {
        refreshUserLastSeen()
    }
}