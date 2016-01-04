package me.alexeyterekhov.vkfilter.Test

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.AudioAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.DocAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.VideoAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList.ChatAdapter
import me.alexeyterekhov.vkfilter.R
import java.util.*

public class ChatTestActivity: ChatActivity() {
    val tests = arrayListOf(
            Pair("Очистить", { clearList() }),
            Pair("Чтение двух сообщений", { testTwoUnreadMessages() }),
            Pair("Вложения", { testAttachments() }),
            Pair("Сочетания вложений", { testCombinations() }),
            Pair("Даты", { testDates() }),
            Pair("Вставка сообщения", { testOneMsgInsert() }),
            Pair("Изменение последнего сообщения", { testMsgChange() }),
            Pair("Скролл в самый низ", { testScrollDown() }),
            Pair("Скролл к непрочитанному", { testScrollToUnread() }),
            Pair("Скролл в самом верху", { testScrollAtTheTop() }),
            Pair("Скролл при изменении поля ввода", { testEditTestSizeChange() }),
            Pair("Цепочка изменений", { testChainOfChanges() }),
            Pair("Отправка сообщений", { testSendMessage() }),
            Pair("Чтение одного сообщения", { testReadOneMessage() }),
            Pair("Чтение, прокрутка", { testReadAndScroll() }),
            Pair("Чтение, изменение", { testReadAndUpdate() }),
            Pair("Быстрое чтение", { testFastRead() }),
            Pair("Подгрузка после разрыва", { testUpdateAfterDisconnect() })
    )
    var currentTest = 0

    private fun getCache() = MessageCaches.getCache("test", true)
    private fun getAdapter() = (findViewById(R.id.messageList) as RecyclerView).adapter as ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insertTestControls()
        initTestControls()
    }

    private fun insertTestControls() {
        val chatActivity = (findViewById(android.R.id.content) as ViewGroup).getChildAt(0)
        (chatActivity.parent as ViewGroup).removeView(chatActivity)
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

        testButton.text = tests[currentTest].first

        nextButton.setOnClickListener {
            if (currentTest < tests.count() - 1) {
                currentTest += 1
                testButton.text = tests[currentTest].first
            }
        }

        prevButton.setOnClickListener {
            if (currentTest > 0) {
                currentTest -= 1
                testButton.text = tests[currentTest].first
            }
        }

        testButton.setOnClickListener {
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
            msgs.forEach {
                it.attachments.messages.add(attachedMsg)
            }
            messages.addAll(msgs)
        }
        // Attached image
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs.forEach {
                it.attachments.images.add(image)
            }
            messages.addAll(msgs)
        }
        // Attached audio
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            msgs.forEach {
                it.attachments.audios.add(audio)
            }
            messages.addAll(msgs)
        }
        // Attached doc
        run {
            val msgs = generateAllTypesOfMessages()
            val doc = DocAttachment("Название документа", 18 * 1024, "abc")
            msgs.forEach {
                it.attachments.documents.add(doc)
            }
            messages.addAll(msgs)
        }
        // Attached video
        run {
            val msgs = generateAllTypesOfMessages()
            val video = VideoAttachment(1, "Название видео", 312, "http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "")
            msgs.forEach {
                it.attachments.videos.add(video)
            }
            messages.addAll(msgs)
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
            msgs.forEach {
                it.text = "Вертикальное расстояние между вложениями"
                it.attachments.audios.add(audio1)
                it.attachments.audios.add(audio2)
            }
            messages.addAll(msgs)
        }
        // Text width vs attachment width
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            msgs.forEach {
                it.text = "Small"
                it.attachments.audios.add(audio)
            }
            messages.addAll(msgs)
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val audio = AudioAttachment("A", "B", 192, "abc")
            msgs.forEach {
                it.text = "Veeeeeeeeryyyyyy bbbiiiiiiiggggg teeeeeeeexxxxttttt"
                it.attachments.audios.add(audio)
            }
            messages.addAll(msgs)
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs.forEach {
                it.text = "Small"
                it.attachments.images.add(image)
            }
            messages.addAll(msgs)
        }
        run {
            val msgs = generateAllTypesOfMessages()
            val image = ImageAttachment("http://cs7060.vk.me/c621626/v621626873/2596c/SWuM7WvcR24.jpg", "", 400, 300)
            msgs.forEach {
                it.text = "Veeeeeeeeryyyyyy bbbiiiiiiiggggg teeeeeeeexxxxttttt"
                it.attachments.images.add(image)
            }
            messages.addAll(msgs)
        }
        // Attachment width vs attachment width
        run {
            val msgs = generateAllTypesOfMessages()
            val audio1 = AudioAttachment("Имя исполнителя песни", "Название композиции", 192, "abc")
            val audio2 = AudioAttachment("A", "B", 192, "abc")
            msgs.forEach {
                it.text = "Audio width vs"
                it.attachments.audios.add(audio1)
                it.attachments.audios.add(audio2)
            }
            messages.addAll(msgs)
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
        dates.reversed().forEachIndexed { ind, date ->
            val time = date
            val msgs = generateAllTypesOfMessages()
            msgs.forEachIndexed { ind2, msg ->
                msg.sentTimeMillis = time
                msg.sentId = (ind * 10 + ind2).toLong()
            }
            messages.addAll(msgs)
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
        if (getCache().getMessages().size == 0)
            (1..20).forEach {
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
            (1..20).forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = true
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
    }

    private fun testScrollToUnread() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..20).forEach {
                val m = Message("me")
                m.sentId = it.toLong()
                m.isIn = true
                m.text = "Старое сообщение $it"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = true
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..10).forEach {
                val m = Message("me")
                m.sentId = it.toLong() + 20
                m.isIn = true
                m.text = "Новое прочитанное сообщение $it"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = true
                messages.add(m)
            }
            (1..30).forEach {
                val m = Message("me")
                m.sentId = it.toLong() + 30
                m.isIn = true
                m.text = "Новое непрочитанное сообщение $it"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = false
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 1000)
    }

    private fun testScrollAtTheTop() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..20).forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = false
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
    }

    private fun testEditTestSizeChange() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..20).forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение"
                m.sentTimeMillis = System.currentTimeMillis()
                m.sentState = Message.STATE_SENT
                m.isRead = true
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
        for (i in 1..5) {
            Handler().postDelayed({
                val line = "abcde"
                var text = ""
                (1..i).forEach {
                    text += "$line${if (it == i) "" else "\n" }"
                }
                (findViewById(R.id.messageText) as EditText).setText(text)
            }, 500L + 500 * i)
        }
        for (i in 6..9) {
            Handler().postDelayed({
                val line = "abcde"
                var text = ""
                (1..(10 - i)).forEach {
                    text += "$line${if (it == 10 - i) "" else "\n" }"
                }
                (findViewById(R.id.messageText) as EditText).setText(text)
            }, 500L + 500 * i)
        }
    }

    private fun testChainOfChanges() {
        clearList()
        Handler().postDelayed({
            val m = Message("me")
            m.isIn = true
            m.text = "Один"
            m.sentId = 1
            m.isRead = true
            getCache().putMessages(Collections.singleton(m), true)
        }, 500)
        for (i in 1..10) {
            Handler().postDelayed({
                val m = getCache().getMessages().first()
                m.text = "Номер $i"
                getCache().onUpdateMessages(Collections.singleton(m))
            }, 500L + 100 * i)
        }
    }

    private fun testSendMessage() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..20).forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение"
                m.sentTimeMillis = System.currentTimeMillis() - 25 * 60 * 60 * 1000
                m.sentState = Message.STATE_SENT
                m.isRead = false
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
        Handler().postDelayed({
            val m = getCache().getEditMessage()
            m.text = "Долго отправляемое сообщение"
            getCache().onWillSendMessage(1L)
        }, 1500)
        Handler().postDelayed({
            getCache().onDidSendMessage(1L, 100L)
        }, 2500)
        Handler().postDelayed({
            val m = getCache().getEditMessage()
            m.text = "Быстро отправляемое сообщение"
            getCache().onWillSendMessage(2L)
        }, 3000)
        Handler().postDelayed({
            getCache().onDidSendMessage(2L, 101L)
        }, 3100)
        Handler().postDelayed({
            val m = getCache().getEditMessage()
            m.text = "Ни туда ни сюда отправляемое сообщение"
            getCache().onWillSendMessage(3L)
        }, 4000)
        Handler().postDelayed({
            getCache().onDidSendMessage(3L, 102L)
        }, 4350)
    }

    private fun testReadOneMessage() {
        clearList()
        Handler().postDelayed({
            val m = Message("me")
            m.isIn = true
            m.text = "Сообщение"
            m.sentTimeMillis = System.currentTimeMillis() - 25 * 60 * 60 * 1000
            m.sentState = Message.STATE_SENT
            m.sentId = 100L
            m.isRead = false
            getCache().putMessages(Collections.singleton(m))
        }, 500)
        Handler().postDelayed({
            getCache().onReadMessages(out = false, lastId = 100L)
        }, 1000)
    }

    private fun testReadAndScroll() {
        clearList()
        Handler().postDelayed({
            val messages = LinkedList<Message>()
            (1..20).forEach {
                val m = Message("me")
                m.isIn = true
                m.text = "Сообщение $it"
                m.sentTimeMillis = System.currentTimeMillis() - 25 * 60 * 60 * 1000
                m.sentState = Message.STATE_SENT
                m.sentId = it.toLong()
                m.isRead = false
                messages.add(m)
            }
            getCache().putMessages(messages)
        }, 500)
        Handler().postDelayed({
            getCache().onReadMessages(out = false, lastId = 20L)
        }, 1000)
        Handler().postDelayed({
            (findViewById(R.id.messageList) as RecyclerView).smoothScrollToPosition(19)
        }, 3000)
    }

    private fun testReadAndUpdate() {
        clearList()
        val m = Message("me")
        m.isIn = true
        m.text = "Сообщение"
        m.sentTimeMillis = System.currentTimeMillis() - 25 * 60 * 60 * 1000
        m.sentState = Message.STATE_SENT
        m.sentId = 100L
        m.isRead = false
        val msgs = Collections.singleton(m)

        Handler().postDelayed({
            getCache().putMessages(Collections.singleton(m))
        }, 500)
        Handler().postDelayed({
            getCache().onReadMessages(out = false, lastId = 100L)
        }, 600)
        for (i in 1..20) {
            Handler().postDelayed({
                getCache().onUpdateMessages(msgs)
            }, 500L + 250 * i)
        }
    }

    private fun testFastRead() {
        val time = System.currentTimeMillis()
        val m = Message("me")
        m.isIn = true
        m.text = "Сообщение"
        m.sentTimeMillis = System.currentTimeMillis() - 25 * 60 * 60 * 1000
        m.sentState = Message.STATE_SENT
        m.sentId = time
        m.isRead = false
        val msgs = Collections.singleton(m)
        getCache().putMessages(msgs)
        Handler().postDelayed({
            getCache().onReadMessages(out = false, lastId = time)
        }, 50)
    }

    private fun testUpdateAfterDisconnect() {
        clearList()
        // Loading messages from server
        run {
            val m1 = generateMessage(out = false)
            val m2 = generateMessage(out = true)
            m1.text = "Подгруженные сообщения"
            m2.text = "Подгруженные сообщения"
            m1.sentId = 1
            m2.sentId = 4
            Handler().postDelayed({ getCache().putMessages(arrayListOf(m1, m2)) }, 500)
        }
        // Here we haven't network
        // Send messages
        run {
            Handler().postDelayed({
                getCache().getEditMessage().text = "Моё сообщение 1     "
                getCache().onWillSendMessage(100)
            }, 1500)
            Handler().postDelayed({
                getCache().getEditMessage().text = "Моё сообщение 2     "
                getCache().onWillSendMessage(101)
            }, 2000)
        }
        // Here we have network again
        // Messages sent
        run {
            Handler().postDelayed({
                getCache().onDidSendMessage(100, 10)
                getCache().onDidSendMessage(101, 11)
            }, 3000)
        }
        // Here dialog refresher loads messages older than sent messages
        run {
            Handler().postDelayed({
                val m1 = generateMessage(out = false)
                val m2 = generateMessage(out = false)
                val m3 = generateMessage(out = false)
                m1.text = "Входящее 1"
                m2.text = "Входящее 2"
                m3.text = "Входящее 3"
                m1.sentId = 6
                m2.sentId = 7
                m3.sentId = 9
                val my1 = generateMessage(out = true)
                val my2 = generateMessage(out = true)
                my1.text = "Моё сообщение 1"
                my2.text = "Моё сообщение 2"
                my1.sentId = 10
                my2.sentId = 11
                getCache().putMessages(arrayListOf(m1, m2, m3, my1, my2))
            }, 4000)
        }
    }

    private fun testTwoUnreadMessages() {
        clearList()
        val messageIn = generateMessage(out = false)
        val messageOut = generateMessage(out = true)
        messageIn.isRead = false
        messageOut.isRead = false
        messageIn.sentId = 1L
        messageOut.sentId = 2L

        Handler().postDelayed({
            getCache().putMessages(arrayListOf(messageIn, messageOut))
        }, 500)
        Handler().postDelayed({
            messageIn.isRead = true
            Log.d("test", "update income message")
            getCache().onUpdateMessages(Collections.singleton(messageIn))
        }, 1000)
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
            generateMessage(true, Message.STATE_SENDING)
    )

    private fun clearList() {
        val adapter = getAdapter()
        adapter.messages.clear()
        MessageCaches.getCache("test", true).clearData()
        adapter.notifyDataSetChanged()
    }
}