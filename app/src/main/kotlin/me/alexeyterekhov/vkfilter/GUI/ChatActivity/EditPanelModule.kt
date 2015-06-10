package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.EditText
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.GUI.Common.KeyboardlessEmojiEditText
import me.alexeyterekhov.vkfilter.R

class EditPanelModule(val activity: ChatActivity) {
    private var bindAction: (() -> Unit)? = null

    val textListener = createTextListener()

    fun onCreate() {
        fillMessageInput()
        initSendButton()
    }

    fun onDestroy() {
        val text = activity.findViewById(R.id.messageText) as EditText
        text.removeTextChangedListener(textListener)
    }

    fun bindSendButton(iconRes: Int, action: () -> Unit, animate: Boolean = false) {
        bindAction = action
        if (animate)
            animateSendButtonIconChange(iconRes)
        else
            activity.findViewById(R.id.sendButton) as ImageView setImageResource iconRes
    }
    fun unbindSendButton(animate: Boolean = false) {
        bindAction = null
        if (animate)
            animateSendButtonIconChange(R.drawable.button_send)
        else
            activity.findViewById(R.id.sendButton) as ImageView setImageResource R.drawable.button_send
    }
    fun getEditText() = activity.findViewById(R.id.messageText) as KeyboardlessEmojiEditText

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
            if (bindAction == null) {
                val editMessage = getMessageCache().getEditMessage()
                if (editMessage.text != "") {
                    activity.requestModule.sendMessage(editMessage)
                    fillMessageInput()
                }
            } else {
                bindAction!!()
            }
        }
    }

    private fun getMessageCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )

    private fun createTextListener() = object : TextWatcher {
        var textInput: EditText? = null

        override fun afterTextChanged(s: Editable?) {
            if (textInput == null)
                textInput = activity.findViewById(R.id.messageText) as EditText
            val editMessage = getMessageCache().getEditMessage()
            editMessage.text = textInput!!.getText().toString()
            if (activity.swipePanelModule.isPanelShown())
                (activity.findViewById(R.id.sendButton) as ImageView).performClick()
        }
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    }

    private fun animateSendButtonIconChange(changeImgToRes: Int) {
        val button = activity.findViewById(R.id.sendButton) as ImageView

        val downScale = ScaleAnimation(
                1.0f, 0f,
                1.0f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        downScale setInterpolator FastOutSlowInInterpolator()
        downScale setDuration 100

        val upScale = ScaleAnimation(
                0f, 1.0f,
                0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        upScale setInterpolator FastOutSlowInInterpolator()
        upScale setDuration 100

        downScale setAnimationListener object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                button setImageResource changeImgToRes
                button startAnimation upScale
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        }
        button startAnimation downScale
    }
}