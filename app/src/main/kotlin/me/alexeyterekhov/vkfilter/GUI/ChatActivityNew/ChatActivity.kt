package me.alexeyterekhov.vkfilter.GUI.ChatActivityNew

import android.content.Intent
import android.os.Bundle
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogListActivity
import me.alexeyterekhov.vkfilter.NotificationService.NotificationMaker
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


public class ChatActivity: VkActivity() {
    val launchParameters = IntentParametersModule(this)
    val listModule = MessageListModule(this)
    val editPanelModule = EditPanelModule(this)
    val requestModule = RequestModule(this)
    val actionBarModule = ActionBarModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_new)
        listModule.onCreate()
        editPanelModule.onCreate()
        actionBarModule.onCreate()
        requestModule.loadDialogPartners()
    }
    override fun onResume() {
        super.onResume()
        listModule.onResume()
        actionBarModule.onResume()

        if (launchParameters.isChat())
            NotificationMaker.clearChatNotifications(launchParameters.dialogId(), AppContext.instance)
        else
            NotificationMaker.clearDialogNotifications(launchParameters.dialogId(), AppContext.instance)
    }
    override fun onPause() {
        super.onPause()
        listModule.onPause()
        actionBarModule.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        listModule.onDestroy()
        editPanelModule.onDestroy()
    }
    override fun onBackPressed() {
        if (launchParameters.isLaunchedFromNotification()) {
            startActivity(Intent(AppContext.instance, javaClass<DialogListActivity>()))
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
            finish()
        } else {
            super<VkActivity>.onBackPressed()
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
        }
    }
}


//        :EmojiconGridFragment.OnEmojiconClickedListener,
//        :EmojiconsFragment.OnEmojiconBackspaceClickedListener
//
//    override fun onEmojiconClicked(emoji: Emojicon?) {
//        val text = findViewById(R.id.messageText) as EditText
//        allowHideEmoji = false
//        EmojiconsFragment.input(text, emoji)
//        allowHideEmoji = true
//    }
//
//    override fun onEmojiconBackspaceClicked(p0: View?) {
//        val text = findViewById(R.id.messageText) as EditText
//        allowHideEmoji = false
//        EmojiconsFragment.backspace(text)
//        allowHideEmoji = true
//    }
//
//    private fun showEmoji() {
//        val container = findViewById(R.id.emoji_container)
//        val animation = AlphaAnimation(0f, 1f)
//        animation setDuration 200L
//        container setVisibility View.VISIBLE
//        container startAnimation animation
//    }
//
//    private fun hideEmoji() {
//        val container = findViewById(R.id.emoji_container)
//        if (container.getVisibility() == View.VISIBLE) {
//            allowHideEmoji = false
//            val animation = AlphaAnimation(1f, 0f)
//            animation setDuration 200L
//            animation setAnimationListener object : Animation.AnimationListener {
//                override fun onAnimationStart(p0: Animation?) {
//                }
//
//                override fun onAnimationEnd(p0: Animation?) {
//                    container setVisibility View.INVISIBLE
//                    allowHideEmoji = true
//                }
//
//                override fun onAnimationRepeat(p0: Animation?) {
//                }
//            }
//            container startAnimation animation
//        }
//    }
//}