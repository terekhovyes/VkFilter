package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.content.*
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import com.rockerhieu.emojicon.EmojiconGridFragment
import com.rockerhieu.emojicon.EmojiconsFragment
import com.rockerhieu.emojicon.emoji.Emojicon
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.DataSaver
import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.MessageCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList.MessageListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogListActivity
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import me.alexeyterekhov.vkfilter.Internet.DialogRefresher
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestControl
import me.alexeyterekhov.vkfilter.NotificationService.NotificationMaker
import me.alexeyterekhov.vkfilter.R

class ChatActivity:
        VkActivity(),
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener
{
    companion object {
        val KEY_SAVED = "ChatActivitySaved"
        val KEY_ADAPTER = "ChatActivityAdapter"
        val KEY_INDEX = "ChatActivityIndex"
        val KEY_TOP = "ChatActivityTop"

        val INTENT_CHAT_ID = "chat_id"
        val INTENT_USER_ID = "user_id"
        val INTENT_TITLE = "title"
        val INTENT_FROM_NOTIFICATION = "from_notification"
    }

    val MESSAGE_PORTION = 40
    private val DIALOG_LOAD_VALUE = 20

    private var id = ""
    private var isChat = false
    private var title = ""
    private var loadingMessages: Boolean = false
    private var allMessagesGot = false
    private var adapterIsEmpty = true

    private var allowHideEmoji = true

    private val messageCacheListener = createMessageListener()
    private val userCacheListener = createUserListener()

    /*
    Activity methods
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super<VkActivity>.onCreate(savedInstanceState)
        id = idFromIntent()
        isChat = isChatFromIntent()
        title = titleFromIntent()
        initActionBar()
        setContentView(R.layout.activity_chat)
        initUIControls()
        initAdapter()
        MessageCache.getDialog(id, isChat).listeners add messageCacheListener
        UserCache.listeners add userCacheListener
        loadUsersIfNotLoaded()
    }

    override fun onResume() {
        super<VkActivity>.onResume()
        refreshAdapterImageSize()
        initAdapter()
        VkRequestControl.resume()
        if (!Mocker.MOCK_MODE)
            DialogRefresher.start(id, isChat)
        else
            messageCacheListener.onDataUpdate()
        if (!isChat) updateUserLastSeen()
        if (isChat)
            NotificationMaker.clearChatNotifications(id, AppContext.instance)
        else
            NotificationMaker.clearDialogNotifications(id, AppContext.instance)
    }

    override fun onPause() {
        super<VkActivity>.onPause()
        if (!Mocker.MOCK_MODE)
            DialogRefresher.stop()
        VkRequestControl.pause()
    }

    override fun onDestroy() {
        super<VkActivity>.onDestroy()
        UserCache.listeners remove userCacheListener
        MessageCache.getDialog(id, isChat).listeners remove messageCacheListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super<VkActivity>.onSaveInstanceState(outState)

        val list = findViewById(R.id.messageList) as ListView
        val index = list.getFirstVisiblePosition()
        val view = list.getChildAt(0)
        val viewTop = if (view == null) 0 else view.getTop()

        with (DataSaver) {
            putObject(KEY_SAVED, true)
            putObject(KEY_ADAPTER, getAdapter())
            putObject(KEY_INDEX, index)
            putObject(KEY_TOP, viewTop)
        }
    }

    override fun onBackPressed() {
        if (launchedFromNotification()) {
            startActivity(Intent(AppContext.instance, javaClass<DialogListActivity>()))
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
            finish()
        } else {
            super<VkActivity>.onBackPressed()
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.activity_chat_menu, menu)
        return super<VkActivity>.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            val copyItem = menu.findItem(R.id.copy_text)
            val messageText = findViewById(R.id.messageText) as EditText
            copyItem.setEnabled(messageText.getText().length() > 0)

            val pasteItem = menu.findItem(R.id.paste_text)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            pasteItem.setEnabled(
                    when {
                        !clipboard.hasPrimaryClip() -> false
                        !clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) -> false
                        else -> true
                    }
            )
        }
        return super<VkActivity>.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.copy_text -> {
                copyText()
                return true
            }
            R.id.paste_text -> {
                pasteText()
                return true
            }
            else -> return super<VkActivity>.onOptionsItemSelected(item)
        }
    }

    /*
    Finding and getting
     */
    private fun idFromIntent() = with (getIntent()) {
        getStringExtra(INTENT_USER_ID) ?: getStringExtra(INTENT_CHAT_ID)!!
    }
    private fun isChatFromIntent() = getIntent().hasExtra(INTENT_CHAT_ID)
    private fun titleFromIntent() = getIntent().getStringExtra(INTENT_TITLE)!!
    private fun getAdapter() = (findViewById(R.id.messageList) as ListView).getAdapter() as MessageListAdapter?
    private fun launchedFromNotification() = getIntent().hasExtra(INTENT_FROM_NOTIFICATION)

    /*
    Working with UI
    */
    fun initUIControls() {
        val listView = findViewById(R.id.messageList) as ListView
        val messageText = findViewById(R.id.messageText) as EditText
        val sendButton = findViewById(R.id.sendButton) as ImageView

        listView.setOnScrollListener(createScrollListener())
        listView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (oldBottom != 0) {
                    val dif = oldBottom - bottom
                    listView.scrollBy(0, dif)
                }
            }
        })
        sendButton.setOnClickListener {
            sendMessage()
        }
        messageText setOnLongClickListener {
            view ->
            val container = findViewById(R.id.emoji_container)
            if (container.getVisibility() == View.INVISIBLE)
                showEmoji()
            else
                hideEmoji()
            true
        }
        messageText addTextChangedListener object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                if (allowHideEmoji)
                    hideEmoji()
            }
            override fun afterTextChanged(p0: Editable?) {}
        }
    }

    fun initAdapter() {
        val list = findViewById(R.id.messageList) as ListView
        var adapterLoaded = false

        if ((DataSaver removeObject KEY_SAVED) != null) {
            val adapter = (DataSaver removeObject KEY_ADAPTER) as MessageListAdapter
            if (adapter.id == id && adapter.chat == isChat) {
                adapterLoaded = true
                list setAdapter adapter
                val index = (DataSaver removeObject KEY_INDEX) as Int
                val top = (DataSaver removeObject KEY_TOP) as Int
                list.setSelectionFromTop(index, top)
            }
        }
        if (!adapterLoaded) {
            val size = Point()
            getWindowManager().getDefaultDisplay().getSize(size)

            val adapter = MessageListAdapter(
                    activity = this,
                    id = id,
                    chat = isChat
            )
            adapter.maxImageHeight = size.y * 2 / 3
            adapter.maxImageWidth = size.x * 2 / 3
            list.setAdapter(adapter)
        }
    }

    fun initActionBar() {
        setTitle(title)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
    }

    fun refreshAdapterImageSize() {
        val adapter = getAdapter()
        if (adapter != null) {
            val size = Point()
            getWindowManager().getDefaultDisplay().getSize(size)
            adapter.maxImageHeight = size.y * 3 / 4
            adapter.maxImageWidth = size.x * 3 / 4
        }
    }

    fun updateUserLastSeen() {
        if (Mocker.MOCK_MODE) {
            val u = User()
            u.isOnline = true
            getSupportActionBar().setSubtitle(TextFormat.userOnlineStatus(u))
        } else {
            if (!isChat && UserCache.contains(id)) {
                val u = UserCache.getUser(id)!!
                getSupportActionBar().setSubtitle(TextFormat.userOnlineStatus(u))
            }
        }
    }

    fun createScrollListener() = object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (firstVisibleItem < DIALOG_LOAD_VALUE) {
                val adapter = getAdapter()
                if (adapter != null && adapter.getCount() > 0)
                    loadMoreMessages(adapter.getItem(0)!!.id.toString())
                else
                    loadMoreMessages()
            }
        }
    }

    fun createMessageListener() = object : DataDepend {
        override fun onDataUpdate() {
            loadingMessages = false
            val adapter = getAdapter()!!
            if (adapterIsEmpty) {
                adapterIsEmpty = false
                adapter.notifyOnNewMessages(findViewById(R.id.messageList) as ListView)
            } else {
                with (MessageCache.getDialog(id, isChat)) {
                    if (info.addedMessagesCount > 0) {
                        allMessagesGot = allHistoryLoaded
                        adapter.notifyOnNewMessages(findViewById(R.id.messageList) as ListView)
                    }
                    if (info.markedFrom > 0 && info.markedTo > 0) {
                        adapter.markAsRead(
                                findViewById(R.id.messageList) as ListView,
                                info.markedFrom, info.markedTo, info.markedMessagesAreIncomes)
                    }
                }
            }
            updateUserLastSeen()
        }
    }

    private fun createUserListener() = object : DataDepend {
        override fun onDataUpdate() {
            getAdapter()?.notifyWhenPossible()
        }
    }

    fun copyText() {
        val messageText = findViewById(R.id.messageText) as EditText
        if (messageText.getText().length() > 0) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("vkfilter text", messageText.getText().toString())
            clipboard.setPrimaryClip(clip)
        }
    }

    fun pasteText() {
        val messageText = findViewById(R.id.messageText) as EditText
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.getPrimaryClip().getItemAt(0)
            val text = when {
                clip.getText() != null -> clip.getText()
                clip.getUri() != null -> clip.getUri().toString()
                else -> null
            }
            if (text != null) {
                val start = messageText.getSelectionStart()
                val end = messageText.getSelectionEnd()
                if (start < 0)
                    messageText.append(text)
                else
                    messageText.getText().replace(
                            Math.min(start, end),
                            Math.max(start, end),
                            text
                    )
            }
        }
    }

    fun sendMessage() {
        val messageText = findViewById(R.id.messageText) as EditText
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

    fun loadMoreMessages(lastMessageId: String = "", count: Int = MESSAGE_PORTION, offset: Int = 0) {
        if (!loadingMessages && !allMessagesGot) {
            loadingMessages = true
            if (Mocker.MOCK_MODE)
                return
            RunFun.messageList(
                    dialogId = id,
                    dialogIsChat = isChat,
                    offset = offset,
                    count = count,
                    startMessageId = lastMessageId
            )
        }
    }

    override fun onEmojiconClicked(emoji: Emojicon?) {
        val text = findViewById(R.id.messageText) as EditText
        allowHideEmoji = false
        EmojiconsFragment.input(text, emoji)
        allowHideEmoji = true
    }

    override fun onEmojiconBackspaceClicked(p0: View?) {
        val text = findViewById(R.id.messageText) as EditText
        allowHideEmoji = false
        EmojiconsFragment.backspace(text)
        allowHideEmoji = true
    }

    private fun showEmoji() {
        val container = findViewById(R.id.emoji_container)
        val animation = AlphaAnimation(0f, 1f)
        animation setDuration 200L
        container setVisibility View.VISIBLE
        container startAnimation animation
    }

    private fun hideEmoji() {
        val container = findViewById(R.id.emoji_container)
        if (container.getVisibility() == View.VISIBLE) {
            allowHideEmoji = false
            val animation = AlphaAnimation(1f, 0f)
            animation setDuration 200L
            animation setAnimationListener object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    container setVisibility View.INVISIBLE
                    allowHideEmoji = true
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            }
            container startAnimation animation
        }
    }

    private fun loadUsersIfNotLoaded() {
        if (UserCache.getMe() == null)
            RunFun.getDialogPartners(id.toLong(), isChat)
    }
}