package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.os.Bundle
import android.os.Handler
import android.view.View
import io.codetail.animation.SupportAnimator
import me.alexeyterekhov.vkfilter.GUI.Common.SwipeOpener
import me.alexeyterekhov.vkfilter.R

class SwipePanelModule(val activity: ChatActivity) {
    private var isOpened = false
    val KEY_PANEL_OPENED = "swipe_panel_opened"

    val closeSwipePanelAction: (() -> Unit) = {
        if (activity.emojiconModule.isEmojiconPanelShown())
            activity.emojiconModule.bindForClosingSmiles(animate = true)
        else
            activity.editPanelModule.unbindSendButton(animate = true)
        hidePanel()
    }

    fun onCreate(saved: Bundle?) {
        if (saved != null && saved containsKey KEY_PANEL_OPENED) {
            bindForClosingSwipePanel(animate = false)
            showPanel(animate = false)
        }
        activity.findViewById(R.id.swipeOpener) as SwipeOpener setListener object : SwipeOpener.OpenerListener {
            override fun onOpen() {
                if (!isPanelShown()) {
                    showPanel(animate = true)
                    Handler().postDelayed({ bindForClosingSwipePanel(animate = true) }, 100)
                }
            }
        }
        activity.findViewById(R.id.smileButton) setOnClickListener {
            activity.emojiconModule.openEmojiconPanel()
            hidePanel()
        }
    }
    fun onSaveState(bundle: Bundle?) {
        if (bundle != null) {
            if (isPanelShown())
                bundle.putBoolean(KEY_PANEL_OPENED, true)
        }
    }

    fun isPanelShown() = isOpened
    fun hidePanel() {
        val panel = activity.findViewById(R.id.attachmentsLayout)
        val messageText = activity.findViewById(R.id.messageText)
        isOpened = false

        val animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                panel,
                0,
                messageText.getHeight() / 2,
                messageText.getWidth() * 1.05f,
                0f
        )
        animator setDuration 200
        animator addListener object : SupportAnimator.AnimatorListener {
            override fun onAnimationEnd() {
                panel.setVisibility(View.INVISIBLE)
            }
            override fun onAnimationStart() {}
            override fun onAnimationCancel() {}
            override fun onAnimationRepeat() {}
        }
        animator.start()
    }
    fun showPanel(animate: Boolean = false) {
        val panel = activity.findViewById(R.id.attachmentsLayout)

        if (!animate) {
            panel setVisibility View.VISIBLE
            isOpened = true
        } else {
            val messageText = activity.findViewById(R.id.messageText)
            val animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                    panel,
                    0,
                    messageText.getHeight() / 2,
                    0f,
                    messageText.getWidth() * 1.05f
            )
            animator setDuration 300
            Handler().postDelayed({ isOpened = true }, 300)
            panel setVisibility View.VISIBLE
            animator.start()
        }
    }

    fun bindForClosingSwipePanel(animate: Boolean = false) {
        activity.editPanelModule.bindSendButton(
                iconRes = R.drawable.button_close,
                action = closeSwipePanelAction,
                animate = animate
        )
    }
}