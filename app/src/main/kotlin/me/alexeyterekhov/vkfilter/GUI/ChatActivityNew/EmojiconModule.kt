package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.rockerhieu.emojicon.EmojiconGridFragment
import com.rockerhieu.emojicon.EmojiconsFragment
import com.rockerhieu.emojicon.emoji.Emojicon
import me.alexeyterekhov.vkfilter.R

class EmojiconModule(val activity: ChatActivity) :
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        EmojiconGridFragment.OnEmojiconClickedListener
{
    val DELAY_BEFORE_OPEN = 500L
    val KEY_MODULE_OPENED = "emoji_open"
    private val closeEmojiPanelAction: (() -> Unit) = {
        hidePanel()
        Handler().postDelayed({
            activity.editPanelModule.getEditText().allowKeyboardAndShow(true)
            activity.editPanelModule.unbindSendButton()
        }, DELAY_BEFORE_OPEN)
    }

    fun onCreate(saved: Bundle?) {
        if (saved != null && saved containsKey KEY_MODULE_OPENED) {
            activity.editPanelModule.getEditText().denyKeyboard()
            bindForClosingSmiles()
            showPanel()
        }
    }
    fun onSaveState(bundle: Bundle?) {
        if (bundle != null) {
            if (isEmojiconPanelShown())
                bundle.putBoolean(KEY_MODULE_OPENED, true)
        }
    }

    fun openEmojiconPanel() {
        activity.editPanelModule.getEditText().denyKeyboard()
        Handler().postDelayed({
            showPanel()
            bindForClosingSmiles()
        }, DELAY_BEFORE_OPEN)
    }
    fun bindForClosingSmiles() {
        activity.editPanelModule.bindSendButton(
                iconRes = R.drawable.button_close,
                action = closeEmojiPanelAction
        )
    }

    private fun hidePanel() = activity.findViewById(R.id.emoji_container).setVisibility(View.GONE)
    private fun showPanel() = activity.findViewById(R.id.emoji_container).setVisibility(View.VISIBLE)
    fun isEmojiconPanelShown() = activity.findViewById(R.id.emoji_container).getVisibility() == View.VISIBLE

    override fun onEmojiconBackspaceClicked(p0: View?) = EmojiconsFragment.backspace(activity.editPanelModule.getEditText())
    override fun onEmojiconClicked(emoji: Emojicon?) = EmojiconsFragment.input(activity.editPanelModule.getEditText(), emoji)
}