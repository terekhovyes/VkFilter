package me.alexeyterekhov.vkfilter.NotificationService

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.utils.DiskCacheUtils
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.ChatActivity
import me.alexeyterekhov.vkfilter.GUI.Common.RoundBitmap
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogListActivity
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.R
import java.util.LinkedList


public object NotificationMaker {
    private val ID = 1
    private val notifications = LinkedList<NotificationInfo>()

    fun addNotification(context: Context, info: NotificationInfo) {
        addOrReplace(info)
        updateNotification(context)
    }

    fun clearAllNotifications(context: Context) {
        notifications.clear()
        killNotification(context)
    }
    fun clearChatNotifications(id: String, context: Context) {
        notifications removeAll (notifications filter { it.chatId == id })
        updateNotification(context)
    }
    fun clearDialogNotifications(id: String, context: Context) {
        notifications removeAll (notifications filter { it.chatId == "" && it.senderId == id })
        updateNotification(context)
    }

    private fun updateNotification(context: Context) {
        if (notifications.isNotEmpty())
            showNotification(context)
        else
            killNotification(context)
    }
    private fun showNotification(context: Context) {
        val notification = if (notifications.size() == 1) {
            val n = notifications.first()
            val onClickIntent = createChatActivityIntent(context, n)
            val title = if (n.getName().length() > 18) n.getName(compact = true) else n.getName()
            notificationBase(context, n.senderPhotoUrl)
                    .setContentTitle(title)
                    .setContentText(n.text)
                    .setContentIntent(onClickIntent)
                    .build()
        } else {
            val firstDialog = notifications.last()
            val moreDialogs = notifications.reverse() drop 1 take 3
            val onClickIntent = createDialogListActivityIntent(context)

            val inbox = NotificationCompat.InboxStyle()
            moreDialogs forEach { n -> inbox addLine "${n.getName(compact = true)}: ${n.text}"}
            inbox setSummaryText (TextFormat.newDialogs(context, notifications.size()))
            inbox setBigContentTitle "${firstDialog.getName(compact = true)}: ${firstDialog.text}"

            notificationBase(context, firstDialog.senderPhotoUrl)
                    .setContentTitle("${firstDialog.getName()} ${TextFormat.andMoreDialogs(context, notifications.size() - 1)}")
                    .setContentText(firstDialog.text)
                    .setContentIntent(onClickIntent)
                    .setStyle(inbox)
                    .build()
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ID, notification)
    }
    private fun killNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ID)
    }

    private fun notificationBase(context: Context, photoUrl: String): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context)
                .setColor(context.getResources().getColor(R.color.material_green))
                .setSmallIcon(R.drawable.icon_notification)
                .setDeleteIntent(createDismissIntent(context))
        val photo = loadPhoto(context, photoUrl)
        if (photo != null)
            builder.setLargeIcon(photo)
        if (allowVibration(context)) {
            val arr = LongArray(4)
            arr.set(0, 0)
            arr.set(1, 300)
            arr.set(2, 300)
            arr.set(3, 300)
            builder.setVibrate(arr)
        }
        if (allowSound(context))
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        if (colorLight(context))
            builder.setLights(0xFF00FF91.toInt(), 1000, 5000)
        else
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
        return builder
    }

    private fun addOrReplace(info: NotificationInfo) {
        val forReplace = notifications filter { it canBeReplacedBy info }
        notifications removeAll forReplace
        notifications add info
    }

    private fun loadPhoto(context: Context, url: String): Bitmap? {
        val loader = ImageLoader.getInstance()
        val dc = loader.getDiskCache()
        val mc = loader.getMemoryCache()

        try {
            var loadedBitmap: Bitmap? = null
            val bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(url, mc)
            if (bitmaps.isNotEmpty())
                loadedBitmap = bitmaps.first()

            val file = DiskCacheUtils.findInCache(url, dc)
            if (file != null)
                loadedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath())
            if (loadedBitmap == null)
                loadedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_photo_loading)
            if (loadedBitmap != null) {
                val res = context.getResources()
                val height = res.getDimension(android.R.dimen.notification_large_icon_height).toInt()
                val width = res.getDimension(android.R.dimen.notification_large_icon_width).toInt()
                loadedBitmap = Bitmap.createScaledBitmap(loadedBitmap!!, width * 3 / 4, height * 3 / 4, false)
                loadedBitmap = RoundBitmap make loadedBitmap!!
            }
            return loadedBitmap
        } catch (e: Exception) {}
        return null
    }

    private fun createChatActivityIntent(context: Context, n: NotificationInfo): PendingIntent {
        val chatIntent = Intent(context, javaClass<ChatActivity>())
        if (n.chatId != "") {
            chatIntent.putExtra("chat_id", n.chatId)
            if (n.chatTitle != "")
                chatIntent.putExtra("title", n.chatTitle)
            else
                chatIntent.putExtra("title", "${n.getName(true)} ${context.getString(R.string.and_others)}")
        } else {
            chatIntent.putExtra("user_id", n.senderId)
            chatIntent.putExtra("title", n.getName())
        }
        chatIntent.putExtra("from_notification", true)

        val stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(javaClass<ChatActivity>())
            .addNextIntent(chatIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createDialogListActivityIntent(context: Context): PendingIntent {
        val dialogIntent = Intent(context, javaClass<DialogListActivity>())

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(javaClass<DialogListActivity>())
        stackBuilder.addNextIntent(dialogIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createDismissIntent(context: Context): PendingIntent {
        val intent = Intent(context, javaClass<NotificationDismissBroadcast>())
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun allowVibration(context: Context) = Settings.allowVibration(preferences(context))
    private fun allowSound(context: Context) = Settings.allowSound(preferences(context))
    private fun colorLight(context: Context) = Settings.allowCustomLights(preferences(context))
    private fun preferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
}