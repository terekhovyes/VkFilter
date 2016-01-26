package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.app.TaskStackBuilder
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.utils.DiskCacheUtils
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.Common.RoundBitmap
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogsActivity
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.NotificationService.NotificationDismissBroadcast
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.R

object NotificationUtil {
    fun createChatActivityIntent(context: Context, n: NotificationInfo): PendingIntent {
        val chatIntent = Intent(context, ChatActivity::class.java)
        when {
            n.chatId != "" && n.chatTitle != "" -> {
                chatIntent.putExtra("chat_id", n.chatId)
                chatIntent.putExtra("title", n.chatTitle)
            }
            n.chatId != "" && n.chatTitle == "" -> {
                chatIntent.putExtra("chat_id", n.chatId)
                chatIntent.putExtra("title", "${n.getName(true)} ${context.getString(R.string.notification_label_and_others)}")
            }
            n.chatId == "" -> {
                chatIntent.putExtra("user_id", n.senderId)
                chatIntent.putExtra("title", n.getName(compact = false))
            }
        }
        chatIntent.putExtra("from_notification", true)
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val stackBuilder = TaskStackBuilder.create(context)
                .addParentStack(ChatActivity::class.java)
                .addNextIntent(chatIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun createDialogsActivityIntent(context: Context): PendingIntent {
        val dialogIntent = Intent(context, DialogsActivity::class.java)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(DialogsActivity::class.java)
        stackBuilder.addNextIntent(dialogIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun createDismissIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationDismissBroadcast::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    fun loadPhoto(context: Context, url: String): Bitmap? {
        val loader = ImageLoader.getInstance()
        val dc = loader.diskCache
        val mc = loader.memoryCache

        try {
            var loadedBitmap: Bitmap? = null
            val bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(url, mc)
            if (bitmaps.isNotEmpty())
                loadedBitmap = bitmaps.first()

            if (url != "") {
                val file = DiskCacheUtils.findInCache(url, dc)
                if (file != null)
                    loadedBitmap = BitmapFactory.decodeFile(file.absolutePath)
            }
            if (loadedBitmap == null)
                loadedBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.icon_user_stub)
            if (loadedBitmap != null) {
                val res = context.resources
                val height = res.getDimension(android.R.dimen.notification_large_icon_height).toInt()
                val width = res.getDimension(android.R.dimen.notification_large_icon_width).toInt()
                loadedBitmap = Bitmap.createScaledBitmap(loadedBitmap, width * 3 / 4, height * 3 / 4, false)
                loadedBitmap = RoundBitmap make loadedBitmap!!
            }
            return loadedBitmap
        } catch (e: Exception) {}
        return null
    }

    fun vibrationAllowed(context: Context) = Settings.allowVibration(preferences(context))
    fun soundAllowed(context: Context) = Settings.allowSound(preferences(context))
    fun counterAllowed(context: Context) = Settings.notificationsWithCount(preferences(context))
    fun colorLightAllowed(context: Context) = Settings.allowCustomLights(preferences(context))
    fun preferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
}