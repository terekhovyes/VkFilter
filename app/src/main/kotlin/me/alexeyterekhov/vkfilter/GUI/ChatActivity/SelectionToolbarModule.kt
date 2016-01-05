package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedMessagePack
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.ClipboardUtil
import java.util.*

class SelectionToolbarModule(val activity: ChatActivity) {
    private val selectionChangeAction = {
        val toolbar = findToolbar()
        val ids = getAdapter()!!.getSelectedMessageIds()

        if (ids.isNotEmpty()) {
            val text = "${activity.getString(R.string.chat_label_toolbar_selected)} ${ids.count()}"
            ((toolbar.findViewById(R.id.textSelectedCount)) as TextView).text = text
        }

        when {
            ids.isNotEmpty() && toolbar.visibility != View.VISIBLE -> showToolbar()
            ids.isEmpty() && toolbar.visibility == View.VISIBLE -> hideToolbar()
        }

        val copyVisibility = if (ids.count() == 1) View.VISIBLE else View.GONE
        toolbar.findViewById(R.id.buttonCopyText).visibility = copyVisibility
        toolbar.findViewById(R.id.labelCopyText).visibility = copyVisibility
    }

    fun onCreate() {
        val adapter = getAdapter()!!
        adapter.onSelectionChangeAction = selectionChangeAction
        selectionChangeAction()

        val toolbar = findToolbar()
        toolbar.findViewById(R.id.buttonCopyText).setOnClickListener {
            val id = adapter.getSelectedMessageIds().first()
            val text = adapter.messageById(id).text
            if (text.length > 0) {
                ClipboardUtil.putText(text)
                Toast
                        .makeText(activity, R.string.chat_toast_text_has_been_copied, Toast.LENGTH_SHORT)
                        .show()
                adapter.deselectAllMessages()
            } else {
                Toast
                        .makeText(activity, R.string.chat_toast_message_has_no_text, Toast.LENGTH_SHORT)
                        .show()
            }
        }
        (toolbar.findViewById(R.id.buttonForwardMessages)).setOnClickListener {
            val ids = adapter.getSelectedMessageIds()
            if (ids.count() > 0) {
                val pack = AttachedMessagePack(
                        title = activity.launchParameters.windowTitle(),
                        messageIds = LinkedList(ids),
                        dialogId = activity.launchParameters.dialogId(),
                        isChat = activity.launchParameters.isChat()
                )
                ClipboardUtil.putMessages(pack)
                Toast
                        .makeText(activity, R.string.chat_toast_message_has_been_copied, Toast.LENGTH_SHORT)
                        .show()
                adapter.deselectAllMessages()
            }
        }
        toolbar.findViewById(R.id.buttonHide).setOnClickListener {
            adapter.deselectAllMessages()
        }
    }

    private fun findToolbar() = activity.findViewById(R.id.selectionToolbar)
    private fun getAdapter() = activity.listModule.getAdapter()
    private fun showToolbar() {
        val view = findToolbar()
        val animation = AlphaAnimation(0f, 1f)
        animation.duration = 250
        view.visibility = View.VISIBLE
        view.startAnimation(animation)
    }
    private fun hideToolbar() {
        val view = findToolbar()
        val animation = AlphaAnimation(1f, 0f)
        animation.duration = 250
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.GONE
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(animation)
    }
}