package me.alexeyterekhov.vkfilter.Test

import android.os.Bundle
import android.os.Handler
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
import java.util.Collections
import java.util.LinkedList
import java.util.Random

public class ChatTestActivity: ChatActivity() {
    val tests = arrayListOf(
            Pair("Очистить", { clearList() }),
            Pair("Вложения", { testAttachments() }),
            Pair("Сочетания вложений", { testCombinations() }),
            Pair("Даты", { testDates() }),
            Pair("Вставка сообщения", { testOneMsgInsert() }),
            Pair("Изменение последнего сообщения", { testMsgChange() }),
            Pair("Скролл в самый низ", { testScrollDown() })
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
        val messages = LinkedList<Message>()
        fun generateMessage(out: Boolean, state: Int = Message.STATE_SENT): Message {
            val m = Message("me")
            m.isOut = out
            m.text = "Текст сообщения"
            m.sentTimeMillis = System.currentTimeMillis()
            m.sentState = state
            m.isRead = true
            return m
        }
        // Attached message
        run {
            val msgs = generateAllTypesOfMessages()
            val attachedMsg = generateMessage(true)
            msgs forEach {
                it.attachments.messages add attachedMsg
            }
            messages addAll msgs
        }
        // Attached image
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs forEach {
                it.attachments.images add image
            }
            messages addAll msgs
        }
        // Attached audio
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            msgs forEach {
                it.attachments.audios add audio
            }
            messages addAll msgs
        }
        // Attached doc
        run {
            val msgs = generateAllTypesOfMessages()
            val doc = DocAttachment("Название документа", 18 * 1024, "abc")
            msgs forEach {
                it.attachments.documents add doc
            }
            messages addAll msgs
        }
        // Attached video
        run {
            val msgs = generateAllTypesOfMessages()
            val video = VideoAttachment(1, "Название видео", 312, "http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "")
            msgs forEach {
                it.attachments.videos add video
            }
            messages addAll msgs
        }
        getCache().putMessages(messages, true)
    }

    private fun testCombinations() {
        clearList()
        val messages = LinkedList<Message>()
        // Vertical distance between attachments
        run {
            val msgs = generateAllTypesOfMessages()
            val audio1 = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            val audio2 = AudioAttachment("Имя исполнителя песни2", "Название композиции2", 192, "abc")
            msgs forEach {
                it.text = "Вертикальное расстояние между вложениями"
                it.attachments.audios add audio1
                it.attachments.audios add audio2
            }
            messages addAll msgs
        }
        // Text width vs attachment width
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            msgs forEach {
                it.text = "Small"
                it.attachments.audios add audio
            }
            messages addAll msgs
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("A", "B", 192, "abc")
            msgs forEach {
                it.text = "Veeeeeeeeryyyyyy bbbiiiiiiiggggg teeeeeeeexxxxttttt"
                it.attachments.audios add audio
            }
            messages addAll msgs
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs forEach {
                it.text = "Small"
                it.attachments.images add image
            }
            messages addAll msgs
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs forEach {
                it.text = "Veeeeeeeeryyyyyy bbbiiiiiiiggggg teeeeeeeexxxxttttt"
                it.attachments.images add image
            }
            messages addAll msgs
        }
        // Attachment width vs attachment width
        run {
            val msgs = generateAllTypesOfMessages()
            val audio1 = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            val audio2 = AudioAttachment("A", "B", 192, "abc")
            msgs forEach {
                it.text = "Audio width vs"
                it.attachments.audios add audio1
                it.attachments.audios add audio2
            }
            messages addAll msgs
        }
        getCache().putMessages(messages, true)
    }

    private fun testDates() {
        clearList()
        val messages = LinkedList<Message>()

        // Dates
        val cur = System.currentTimeMillis()
        val dates = arrayListOf(
                cur - 60L * 1000, // 1 min ago
                cur - 24L * 60 * 60 * 1000, // 1 day ago
                cur - 7L * 24 * 60 * 60 * 1000, // 1 week ago
                cur - 30L * 24 * 60 * 60 * 1000, // 1 month ago
                cur - 13L * 30 * 24 * 60 * 60 * 1000 // 1 year ago
        )
        dates.reverse() forEach {
            val time = it
            val msgs = generateAllTypesOfMessages()
            msgs forEach {
                it.sentTimeMillis = time
            }
            messages addAll msgs
        }

        getCache().putMessages(messages, true)
    }

    private fun testOneMsgInsert() {
        val msg = generateMessage(false, Message.STATE_SENT)
        msg.text = "Одно сообщение"
        if (getCache().getMessages().isEmpty())
            msg.sentId = System.currentTimeMillis()
        else
            msg.sentId = getCache().getMessages().last().sentId + 1
        getCache().putMessages(Collections.singleton(msg), true)
    }

    private fun testMsgChange() {
        if (getCache().getMessages().size() == 0)
            1..20 forEach {
                testOneMsgInsert()
            }
        val lastMsg = getCache().getMessages().last()
        val lines = Random(System.currentTimeMillis()).nextInt(5) + 1
        var text = ""
        for (i in 1..lines)
            text += "${System.currentTimeMillis()}${if (i != lines) "\n" else ""}"
        lastMsg.text = text
        getCache().onUpdateMessages(Collections.singleton(lastMsg))
    }

    private fun testScrollDown() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            1..20 forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = true
                messages add m
            }
            getCache().onUpdateMessages(messages)
        }, 500)
    }

    private fun testScrollToUnread() {
        clearList()
    }

    // Util methods

    private fun generateMessage(out: Boolean, state: Int = Message.STATE_SENT): Message {
        val m = Message("me")
        m.isOut = out
        m.text = "Текст сообщения"
        m.sentTimeMillis = System.currentTimeMillis()
        m.sentState = state
        m.isRead = true
        return m
    }
    private fun generateAllTypesOfMessages() = arrayListOf(
            generateMessage(false, Message.STATE_SENT),
            generateMessage(true, Message.STATE_SENT),
            generateMessage(true, Message.STATE_PROCESSING)
    )

    private fun clearList() {
        val adapter = getAdapter()
        adapter.messages.clear()
        MessageCaches.getCache("test", true).clearData()
        adapter.notifyDataSetChanged()
    }
}