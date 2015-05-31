package me.alexeyterekhov.vkfilter.Test

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.ChatActivityNew.MessageList.ChatAdapter
import me.alexeyterekhov.vkfilter.R

public class ChatTestActivity: ChatActivity() {
    var currentTest = 0

    private fun getCache() = MessageCaches.getCache("test", true)
    private fun getAdapter() = (findViewById(R.id.messageList) as RecyclerView)
            .getAdapter() as ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insertTestControls()
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

    private fun testAttachments() {
        clearList()

        fun generateMsg(out: Boolean, state: Int = Message.STATE_SENT): Message {
            val m = Message("me")
            m.isOut = out
            m.text = "Текст сообщения"
            m.sentTimeMillis = System.currentTimeMillis()
            return m
        }

        // With attached message
        val m = generateMsg(true, Message.STATE_SENT)
        m.attachments.messages
        //attachedMessage.is

    }

    private fun clearList() {
        val adapter = getAdapter()
        adapter.messages.clear()
        MessageCaches.deleteCache("test", true)
        adapter.notifyDataSetChanged()
    }
}