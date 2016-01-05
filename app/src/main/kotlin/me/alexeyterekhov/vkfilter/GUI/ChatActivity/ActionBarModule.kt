package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.graphics.Typeface
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.ChatInfo
import me.alexeyterekhov.vkfilter.DataClasses.Device
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarHolder
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestChats
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.TextFormat
import java.util.*

class ActionBarModule(val activity: ChatActivity) {
    val listener = createListener()

    fun onCreate() {
        val toolbar = findToolbar()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateTitle(toolbar)
        updateSubtitle(toolbar)
        updateAvatar(toolbar, activity.launchParameters.isChat(), activity.launchParameters.dialogId())

        if (activity.launchParameters.isChat()) {
            val chatIds = Collections.singleton(activity.launchParameters.dialogId())
            RequestControl.addBackground(RequestChats(chatIds))
        }
    }

    fun onResume() {
        val toolbar = findToolbar()

        updateSubtitle(toolbar)
        updateAvatar(toolbar, activity.launchParameters.isChat(), activity.launchParameters.dialogId())
        UserCache.listeners.add(listener)
        ChatInfoCache.listeners.add(listener)
    }

    fun onPause() {
        UserCache.listeners.remove(listener)
        ChatInfoCache.listeners.remove(listener)
    }

    private fun updateTitle(toolbar: Toolbar) {
        val userName = findTitle(toolbar)

        userName.text = activity.launchParameters.windowTitle()
        userName.typeface = Typeface.createFromAsset(activity.assets, "fonts/Roboto-Medium.ttf")
    }

    private fun updateAvatar(toolbar: Toolbar, isChat: Boolean, dialogId: String) {
        val imageUrls = LinkedList<String>()

        if (isChat) {
            val chat = ChatInfoCache.getChat(dialogId)
            if (chat != null)
                for (i in 0..chat.getImageCount() - 1)
                    imageUrls.add(chat.getImageUrl(i))
        } else {
            val user = UserCache.getUser(dialogId)
            if (user != null)
                imageUrls.add(user.photoUrl)
        }

        updateAvatar(toolbar, imageUrls)
    }

    private fun updateAvatar(toolbar: Toolbar, imageUrls: List<String>) {
        val avatarLayout = toolbar.findViewById(R.id.avatarLayout)
        val avatarHolder = AvatarHolder(avatarLayout)
        val imageLoader = ImageLoader.getInstance()

        when (imageUrls.count()) {
            0 -> {
                avatarHolder.setVisibilityForCount(1)
                avatarHolder.singleImage.setImageResource(R.drawable.icon_user_stub)
            }
            1 -> {
                avatarHolder.setVisibilityForCount(1)
                imageLoader.displayImage(imageUrls[0], avatarHolder.singleImage)
            }
            2 -> {
                avatarHolder.setVisibilityForCount(2)
                imageLoader.displayImage(imageUrls[0], avatarHolder.doubleImage1)
                imageLoader.displayImage(imageUrls[1], avatarHolder.doubleImage2)
            }
            3 -> {
                avatarHolder.setVisibilityForCount(3)
                imageLoader.displayImage(imageUrls[0], avatarHolder.tripleImage1)
                imageLoader.displayImage(imageUrls[1], avatarHolder.tripleImage2)
                imageLoader.displayImage(imageUrls[2], avatarHolder.tripleImage3)
            }
            else -> {
                avatarHolder.setVisibilityForCount(4)
                imageLoader.displayImage(imageUrls[0], avatarHolder.quadImage1)
                imageLoader.displayImage(imageUrls[1], avatarHolder.quadImage2)
                imageLoader.displayImage(imageUrls[2], avatarHolder.quadImage3)
                imageLoader.displayImage(imageUrls[3], avatarHolder.quadImage4)
            }
        }
    }

    private fun updateSubtitle(toolbar: Toolbar) {
        if (activity.launchParameters.isChat()) {
            val chat = ChatInfoCache.getChat(activity.launchParameters.dialogId())
            if (chat != null)
                updateSubtitleMembersCount(toolbar, chat)
            else
                findSubtitle(toolbar).text = ""
        } else {
            val user = UserCache.getUser(activity.launchParameters.dialogId())
            if (user != null)
                updateSubtitleOnlineStatus(toolbar, user)
            else
                findSubtitle(toolbar).text = ""
        }
    }

    private fun updateSubtitleOnlineStatus(toolbar: Toolbar, user: User) {
        val fromPhone = user.deviceType == Device.MOBILE

        with (findSubtitleIcon(toolbar)) {
            visibility = if (fromPhone) View.VISIBLE else View.INVISIBLE
            setImageResource(R.drawable.ic_phone)
        }

        findSubtitle(toolbar).text = TextFormat.userOnlineStatusCompact(user)
    }

    private fun updateSubtitleMembersCount(toolbar: Toolbar, chat: ChatInfo) {
        findSubtitle(toolbar).text = TextFormat.membersPhrase(chat.chatPartners.count())
    }

    private fun createListener() = object : DataDepend {
        override fun onDataUpdate() {
            val toolbar = findToolbar()
            updateSubtitle(toolbar)
            updateAvatar(toolbar, activity.launchParameters.isChat(), activity.launchParameters.dialogId())
        }
    }

    private fun findToolbar() = activity.findViewById(R.id.toolbar) as Toolbar
    private fun findTitle(toolbar: Toolbar) = toolbar.findViewById(R.id.userName) as TextView
    private fun findSubtitle(toolbar: Toolbar) = toolbar.findViewById(R.id.subtitle) as TextView
    private fun findSubtitleIcon(toolbar: Toolbar) = toolbar.findViewById(R.id.subtitleIcon) as ImageView
}