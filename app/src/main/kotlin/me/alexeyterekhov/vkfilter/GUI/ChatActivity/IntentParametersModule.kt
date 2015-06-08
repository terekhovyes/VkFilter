package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.support.v7.app.AppCompatActivity

class IntentParametersModule(val activity: AppCompatActivity) {
    companion object {
        val INTENT_CHAT_ID = "chat_id"
        val INTENT_USER_ID = "user_id"
        val INTENT_TITLE = "title"
        val INTENT_FROM_NOTIFICATION = "from_notification"
    }

    fun isChat() = activity.getIntent().hasExtra(INTENT_CHAT_ID)
    fun isNotChat() = !isChat()
    fun dialogId() = with (activity.getIntent()) {
        getStringExtra(INTENT_USER_ID) ?: getStringExtra(INTENT_CHAT_ID)!!
    }
    fun windowTitle() = activity.getIntent().getStringExtra(INTENT_TITLE)!!
    fun isLaunchedFromNotification() = activity.getIntent().hasExtra(INTENT_FROM_NOTIFICATION)
}