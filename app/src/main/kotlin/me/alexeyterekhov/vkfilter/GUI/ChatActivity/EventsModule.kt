package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.Events.EventUserTyping
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
import me.alexeyterekhov.vkfilter.R
import java.util.*

class EventsModule(val activity: ChatActivity) {
    private val SHOW_TYPING_MILLIS = 8000L

    private val handler = Handler()
    private val cancelTypingRunnables = HashMap<String, Runnable>()

    fun onResume() {
        LongPollControl.start()
        LongPollControl.eventBus().register(this)
    }

    fun onPause() {
        LongPollControl.stop()
        LongPollControl.eventBus().unregister(this)
    }

    public fun onEvent(typingEvent: EventUserTyping) {
        if (activity.launchParameters.isChat() == typingEvent.isChat
            && activity.launchParameters.dialogId() == typingEvent.dialogId) {
            if (cancelTypingRunnables.containsKey(typingEvent.userId)) {
                val cancelTyping = cancelTypingRunnables[typingEvent.userId]
                handler.removeCallbacks(cancelTyping)
                handler.postDelayed(cancelTyping, SHOW_TYPING_MILLIS)
            } else {
                val typingMessage = Message(typingEvent.userId)
                typingMessage.isIn = true
                typingMessage.sentState = Message.STATE_TYPING
                typingMessage.text = "${typingMessage.senderOrEmpty().firstName} ${activity.getString(R.string.chat_label_typing)}".trim()

                val cancelTyping = Runnable {
                    activity.listModule.removeTypingMessage(typingEvent.userId)
                    cancelTypingRunnables.remove(typingEvent.userId)
                }

                cancelTypingRunnables.put(typingEvent.userId, cancelTyping)
                activity.listModule.addTypingMessage(typingMessage)
                handler.postDelayed(cancelTyping, SHOW_TYPING_MILLIS)
            }
        }
    }
}