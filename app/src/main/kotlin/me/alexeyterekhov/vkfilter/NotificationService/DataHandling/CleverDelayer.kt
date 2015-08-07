package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import android.os.Handler
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestCheckMessages
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo

class CleverDelayer(
        val context: Context,
        val notification: NotificationInfo,
        val handler: Handler
) {
    private val DESKTOP_READING_TIME = 5000L
    private val CHECK_REQUEST_TIME = 5000L

    init {
        handler.postDelayed({
            val startTime = System.currentTimeMillis()
            val showNotification = Runnable {
                NotificationCollector.addStupidNotification(context, notification)
            }
            RequestControl addBackground RequestCheckMessages(
                    listOf(notification.messageSentId.toLong()),
                    {
                        readIds ->
                        if (System.currentTimeMillis() - startTime < CHECK_REQUEST_TIME) {
                            handler.removeCallbacks(showNotification)
                            if (readIds.isEmpty())
                                showNotification.run()
                        } else {
                            if (readIds.isNotEmpty())
                                NotificationCollector.removeNotification(context, notification)
                        }
                    }
            )
            handler.postDelayed(showNotification, CHECK_REQUEST_TIME)
        }, DESKTOP_READING_TIME)
    }
}