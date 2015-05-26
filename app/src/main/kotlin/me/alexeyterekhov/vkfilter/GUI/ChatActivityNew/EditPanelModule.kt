package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.R

class EditPanelModule(val activity: ChatActivity) {
    val textListener = createTextListener()

    fun onCreate() {
        fillMessageInput()
        initSendButton()
    }

    fun onDestroy() {
        val text = activity.findViewById(R.id.messageText) as EditText
        text.removeTextChangedListener(textListener)
    }

    private fun fillMessageInput() {
        val text = activity.findViewById(R.id.messageText) as EditText
        text.removeTextChangedListener(textListener)
        val editMessage = getMessageCache().getEditMessage()
        text.setText(editMessage.text)
        text.setSelection(editMessage.text.count())
        text.addTextChangedListener(textListener)
    }

    private fun initSendButton() {
        val sendButton = activity.findViewById(R.id.sendButton) as ImageView
        sendButton setOnClickListener {
            val editMessage = getMessageCache().getEditMessage()
            if (editMessage.text != "") {
                activity.requestModule.sendMessage(editMessage)
                fillMessageInput()
            }
        }
    }

    private fun saveEditMessage() {
        val editMessage = getMessageCache().getEditMessage()
        val textInput = activity.findViewById(R.id.messageText) as EditText
        editMessage.text = textInput.getText().toString()
    }

    private fun getMessageCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )

    private fun createTextListener() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = saveEditMessage()
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    }
}