package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.EditText
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedImages
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.GUI.Common.KeyboardlessEmojiEditText
import me.alexeyterekhov.vkfilter.R

class EditPanelModule(val activity: ChatActivity) {
    private var bindAction: (() -> Unit)? = null
    private var autoSending = false
    val textListener = createTextListener()
    val uploadListener = createImageUploadListener()

    fun onCreate() {
        fillMessageInput()
        initSendButton()
        with (getAttachedCache().images) {
            listeners add uploadListener
            if (sendMessageAfterUploading) {
                sendMessageAfterUploading = false
                autoSending = true
                (activity.findViewById(R.id.sendButton) as ImageView) setImageResource R.drawable.button_loading
            }
        }
    }

    fun onDestroy() {
        val text = activity.findViewById(R.id.messageText) as EditText
        text.removeTextChangedListener(textListener)
        with (getAttachedCache().images) {
            listeners remove uploadListener
            if (autoSending)
                sendMessageAfterUploading = true
        }
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
        val icon = if (autoSending) R.drawable.button_loading else R.drawable.button_send
        if (animate)
            animateSendButtonIconChange(icon)
        else
            activity.findViewById(R.id.sendButton) as ImageView setImageResource icon
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
            when {
                bindAction != null -> bindAction!!()
                mTextIsNotEmpty() && !mHasAttachments() -> sendMessage()
                mHasAttachments() && mHasUploadedImages() -> sendMessage()
                mHasAttachments() && !mHasUploadedImages() -> {
                    if (autoSending)
                        disableAutoSending()
                    else
                        enableAutoSending()
                }
            }
        }
    }

    private fun enableAutoSending() {
        autoSending = true
        animateSendButtonIconChange(R.drawable.button_loading)
    }

    private fun disableAutoSending() {
        autoSending = false
        animateSendButtonIconChange(R.drawable.button_send)
    }

    private fun sendMessage() {
        activity.requestModule.sendMessage(getMessageCache().getEditMessage())
        fillMessageInput()
        if (autoSending)
            disableAutoSending()
    }

    private fun getMessageCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )
    private fun getAttachedCache() = AttachedCache.get(
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

    private fun createImageUploadListener() = object : AttachedImages.AttachedImageListener {
        override fun onFinish(image: ImageUpload) {
            if (autoSending && mHasUploadedImages())
                sendMessage()
        }
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

    private fun mTextIsNotEmpty() = getMessageCache().getEditMessage().text.isNotBlank()
    private fun mHasAttachments(): Boolean {
        val hasImages = getAttachedCache()
                .images
                .uploads
                .isNotEmpty()
        return hasImages
    }
    private fun mHasUploadedImages(): Boolean {
        return getAttachedCache()
                .images
                .uploads
                .all { it.state == ImageUpload.STATE_UPLOADED }
    }
}