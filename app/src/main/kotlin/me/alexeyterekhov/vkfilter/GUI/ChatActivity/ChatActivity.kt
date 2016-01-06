package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.rockerhieu.emojicon.EmojiconGridFragment
import com.rockerhieu.emojicon.EmojiconsFragment
import com.rockerhieu.emojicon.emoji.Emojicon
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogsActivity
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.NotificationCollector
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


open public class ChatActivity:
        VkActivity(),
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        EmojiconGridFragment.OnEmojiconClickedListener
{
    val launchParameters = IntentParametersModule(this)
    val listModule = MessageListModule(this)
    val editPanelModule = EditPanelModule(this)
    val requestModule = RequestModule(this)
    val actionBarModule = ActionBarModule(this)
    val emojiconModule = EmojiconModule(this)
    val swipePanelModule = SwipePanelModule(this)
    val uploadModule = UploadModule(this)
    val attachmentsModule = AttachmentsBarModule(this)
    val selectionToolbarModule = SelectionToolbarModule(this)
    val refreshIndicatorModule = RefreshIndicatorModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super<VkActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        listModule.onCreate()
        editPanelModule.onCreate()
        actionBarModule.onCreate()
        requestModule.loadDialogPartners()
        emojiconModule.onCreate(savedInstanceState)
        swipePanelModule.onCreate(savedInstanceState)
        attachmentsModule.onCreate()
        selectionToolbarModule.onCreate()
        uploadModule.onCreate()
        refreshIndicatorModule.onCreate()
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super<VkActivity>.onRestoreInstanceState(savedInstanceState)
        listModule.onRestoreState()
    }
    override fun onResume() {
        super<VkActivity>.onResume()
        listModule.onResume()
        actionBarModule.onResume()
        swipePanelModule.onResume()
        NotificationCollector.removeNotification(AppContext.instance, launchParameters.dialogId(), launchParameters.isChat())
    }
    override fun onPause() {
        super<VkActivity>.onPause()
        listModule.onPause()
        actionBarModule.onPause()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super<VkActivity>.onSaveInstanceState(outState)
        emojiconModule.onSaveState(outState)
        swipePanelModule.onSaveState(outState)
        listModule.onSaveState()
        uploadModule.onSaveState()
    }
    override fun onDestroy() {
        super<VkActivity>.onDestroy()
        listModule.onDestroy()
        editPanelModule.onDestroy()
        attachmentsModule.onDestroy()
    }
    override fun onBackPressed() {
        if (launchParameters.isLaunchedFromNotification()) {
            startActivity(Intent(AppContext.instance, DialogsActivity::class.java))
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
            finish()
        } else {
            super<VkActivity>.onBackPressed()
            overridePendingTransition(R.anim.activity_from_left, R.anim.activity_to_right)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super<VkActivity>.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super<VkActivity>.onActivityResult(requestCode, resultCode, data)
        uploadModule.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        swipePanelModule.onRequestPermissions(requestCode)
    }

    override fun onEmojiconBackspaceClicked(v: View?) = emojiconModule.onEmojiconBackspaceClicked(v)
    override fun onEmojiconClicked(emoji: Emojicon?) = emojiconModule.onEmojiconClicked(emoji)
}