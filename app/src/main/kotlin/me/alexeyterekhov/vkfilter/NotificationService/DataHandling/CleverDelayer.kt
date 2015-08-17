package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestCheckMessages
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.SleepWorker

class CleverDelayer(
        val context: Context,
        val notification: NotificationInfo
) {
    private val DESKTOP_READING_TIME = 5000L
    private val CHECK_REQUEST_TIME = 5000L

    init {
        SleepWorker.addWork(Runnable {
            val startTime = System.currentTimeMillis()
            val showNotification = Runnable {
                NotificationCollector.addStupidNotification(context, notification)
            }

            RequestControl addBackground RequestCheckMessages(
                    listOf(notification.messageSentId.toLong()),
                    {
                        readIds ->
                        if (System.currentTimeMillis() - startTime < CHECK_REQUEST_TIME) {
                            SleepWorker.cancelWork(showNotification)
                            if (readIds.isEmpty())
                                showNotification.run()
                        } else {
                            if (readIds.isNotEmpty())
                                NotificationCollector.removeNotification(context, notification)
                        }
                    }
            )
            SleepWorker.addWork(showNotification, CHECK_REQUEST_TIME)
        }, DESKTOP_READING_TIME)
    }
}