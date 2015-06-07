package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import android.os.Bundle
import android.view.View
import me.alexeyterekhov.vkfilter.GUI.Common.SwipeOpener
import me.alexeyterekhov.vkfilter.R

class SwipePanelModule(val activity: ChatActivity) {
    val KEY_PANEL_OPENED = "swipe_panel_opened"

    val closeSwipePanelAction: (() -> Unit) = {
        if (activity.emojiconModule.isEmojiconPanelShown())
            activity.emojiconModule.bindForClosingSmiles()
        else
            activity.editPanelModule.unbindSendButton()
        closeWithAnimation()
    }

    fun onCreate(saved: Bundle?) {
        if (saved != null && saved containsKey KEY_PANEL_OPENED) {
            bindForClosingSwipePanel()
            activity.findViewById(R.id.barBackground) setVisibility View.VISIBLE
            activity.findViewById(R.id.smileButton) setVisibility View.VISIBLE
        }
        activity.findViewById(R.id.swipeOpener) as SwipeOpener setListener object : SwipeOpener.OpenerListener {
            override fun onOpen() {
                if (!isPanelOpened()) {
                    activity.findViewById(R.id.barBackground) setVisibility View.VISIBLE
                    activity.findViewById(R.id.smileButton) setVisibility View.VISIBLE
                    bindForClosingSwipePanel()
                }
            }
        }
        activity.findViewById(R.id.smileButton) setOnClickListener {
            activity.emojiconModule.openEmojiconPanel()
            closeWithAnimation()
        }
    }
    fun onSaveState(bundle: Bundle?) {
        if (bundle != null) {
            if (isPanelOpened())
                bundle.putBoolean(KEY_PANEL_OPENED, true)
        }
    }

    fun isPanelOpened() = activity.findViewById(R.id.barBackground).getVisibility() == View.VISIBLE
    fun closeWithAnimation() {
        activity.findViewById(R.id.barBackground) setVisibility View.INVISIBLE
        activity.findViewById(R.id.smileButton) setVisibility View.INVISIBLE
    }
    fun bindForClosingSwipePanel() {
        activity.editPanelModule.bindSendButton(
                iconRes = R.drawable.button_close,
                action = closeSwipePanelAction
        )
    }
}