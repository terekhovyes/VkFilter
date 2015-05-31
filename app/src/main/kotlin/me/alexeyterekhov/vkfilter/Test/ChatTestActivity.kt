package me.alexeyterekhov.vkfilter.Test

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.AudioAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.DocAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.VideoAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.MessageList.ChatAdapter
import me.alexeyterekhov.vkfilter.R
import java.util.LinkedList

public class ChatTestActivity: ChatActivity() {
    val tests = arrayListOf(
            Pair("Вложения", { testAttachments() })
    )
    var currentTest = 0

    private fun getCache() = MessageCaches.getCache("test", true)
    private fun getAdapter() = (findViewById(R.id.messageList) as RecyclerView)
            .getAdapter() as ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insertTestControls()
        initTestControls()
    }

    private fun insertTestControls() {
        val chatActivity = findViewById(android.R.id.content) as ViewGroup getChildAt 0
        (chatActivity.getParent() as ViewGroup).removeView(chatActivity)
        val inflater = LayoutInflater.from(this)
        val testActivity = inflater.inflate(R.layout.activity_test_chat, null, false)
        val container = testActivity.findViewById(R.id.contentLayout) as FrameLayout
        container.addView(chatActivity)
        setContentView(testActivity)
    }

    private fun initTestControls() {
        val nextButton = findViewById(R.id.nextButton) as Button
        val prevButton = findViewById(R.id.prevButton) as Button
        val testButton = findViewById(R.id.testButton) as Button

        testButton setText tests[currentTest].first

        nextButton setOnClickListener {
            if (currentTest < tests.count() - 1) {
                currentTest += 1
                testButton setText tests[currentTest].first
            }
        }

        prevButton setOnClickListener {
            if (currentTest > 0) {
                currentTest -= 1
                testButton setText tests[currentTest].first
            }
        }

        testButton setOnClickListener {
            tests[currentTest].second()
        }
    }

    private fun testAttachments() {
        clearList()

        fun generateMsg(out: Boolean, state: Int = Message.STATE_SENT): Message {
            val m = Message("me")
            m.isOut = out
            m.text = "Текст сообщения"
            m.sentTimeMillis = System.currentTimeMillis()
            m.sentState = state
            m.isRead = true
            return m
        }
        fun generateMsgs() = arrayListOf(
                generateMsg(true, Message.STATE_PROCESSING),
                generateMsg(true, Message.STATE_SENT),
                generateMsg(false, Message.STATE_SENT)
        )

        val messages = LinkedList<Message>()

        // Attached message
        run {
            val msgs = generateMsgs()
            val attachedMsg = generateMsg(true)
            msgs forEach {
                it.attachments.messages add attachedMsg
            }
            messages addAll msgs
        }
        // Attached image
        run {
            val msgs = generateMsgs()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs forEach {
                it.attachments.images add image
            }
            messages addAll msgs
        }
        // Attached audio
        run {
            val msgs = generateMsgs()
            val audio = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            msgs forEach {
                it.attachments.audios add audio
            }
            messages addAll msgs
        }
        // Attached doc
        run {
            val msgs = generateMsgs()
            val doc = DocAttachment("Название документа", 18 * 1024, "abc")
            msgs forEach {
                it.attachments.documents add doc
            }
            messages addAll msgs
        }
        // Attached video
        run {
            val msgs = generateMsgs()
            val video = VideoAttachment(1, "Название видео", 312, "http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "")
            msgs forEach {
                it.attachments.videos add video
            }
            messages addAll msgs
        }

        getCache().putMessages(messages, true)
    }

    private fun clearList() {
        val adapter = getAdapter()
        adapter.messages.clear()
        MessageCaches.getCache("test", true).clearData()
        adapter.notifyDataSetChanged()
    }
}