package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestCheckMessages
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.SleepWorker
import java.util.LinkedList

object NotificationCollector {
    private val notifications = LinkedList<NotificationInfo>()
    private var checkScheduler: CheckScheduler? = null

    fun addNotification(context: Context, notification: NotificationInfo) {
        if (isCleverMode())
            CleverDelayer(context, notification)
        else
            addStupidNotification(context, notification)
    }
    fun addStupidNotification(context: Context, notification: NotificationInfo) {
        val forReplace = notifications filter { it canBeReplacedBy notification }
        notifications removeAll forReplace
        notifications add notification
        onDataChanged(context)
    }
    fun removeNotification(context: Context, notification: NotificationInfo) {
        notifications remove notification
        onDataChanged(context)
    }
    fun removeNotification(context: Context, dialogId: String, isChat: Boolean) {
        if (isChat)
            notifications removeAll (notifications filter { it.chatId == dialogId })
        else
            notifications removeAll (notifications filter { it.chatId == "" && it.senderId == dialogId })
        onDataChanged(context)
    }
    fun removeAllNotifications(context: Context) {
        notifications.clear()
        onDataChanged(context)
    }

    fun onDataChanged(context: Context) {
        NotificationMaker.showNotificationFor(context, notifications)
        scheduleMessageCheck(context)
    }
    fun scheduleMessageCheck(context: Context) {
        if (!isCleverMode())
            return
        if (notifications.isEmpty()) {
            if (checkScheduler != null)
                checkScheduler!!.cancel()
            return
        }
        if (checkScheduler == null)
            checkScheduler = CheckScheduler(context)

        checkScheduler!!.start()
    }

    private fun isCleverMode() = Settings.getCleverNotificationsEnabled()

    private class CheckIntervals {
        private val intervals = arrayListOf(
                Pair(10000L, 6), // 0-1 minutes by 10 seconds
                Pair(60000L, 4), // 1-5 minutes by 1 minute
                Pair(5 * 60000L, 1), // 5-10 minutes by 5 minutes
                Pair(10 * 60000L, 5) // 10-60 minutes by 10 minutes
        )
        private var currentIntervalIndex = 0
        private var currentIntervalRepeats = 0

        fun reset() {
            currentIntervalIndex = 0
            currentIntervalRepeats = 0
        }

        fun hasNext(): Boolean {
            val lastIntervalIndex = intervals.count() - 1
            val maxRepeats = intervals[intervals.count() - 1].second
            return !(currentIntervalIndex == lastIntervalIndex && currentIntervalRepeats == maxRepeats)
        }
        fun getNextIntervalMillis(): Long {
            val interval = intervals[currentIntervalIndex].first
            currentIntervalRepeats++
            if (currentIntervalRepeats >= intervals[currentIntervalIndex].second
                && currentIntervalIndex + 1 < intervals.count()) {
                currentIntervalIndex++
                currentIntervalRepeats = 0
            }
            return interval
        }
    }

    private class CheckScheduler(val context: Context) {
        private var scheduled = false
        private val intervals = CheckIntervals()
        private val checkMessagesWork = Runnable {
            val messageIds = notifications map { it.messageSentId.toLong() }
            RequestControl addBackground RequestCheckMessages(messageIds, { readIds ->
                scheduled = false
                if (readIds.isNotEmpty()) {
                    notifications removeAll (notifications filter { readIds contains it.messageSentId.toLong() })
                    onDataChanged(context)
                } else
                    continueChecking()
            })
        }

        private fun continueChecking() {
            if (intervals.hasNext()) {
                scheduled = true
                SleepWorker.addWork(checkMessagesWork, intervals.getNextIntervalMillis())
            }
        }

        fun start() {
            if (scheduled)
                cancel()
            intervals.reset()
            continueChecking()
        }

        fun cancel() {
            if (scheduled) {
                scheduled = false
                SleepWorker.cancelWork(checkMessagesWork)
            }
        }
    }
}