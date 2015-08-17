package me.alexeyterekhov.vkfilter.NotificationService

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.LinkedList

object SleepWorker {
    private val works = SleepCollection()
    private var lastIntent: PendingIntent? = null
    var context: Context? = null

    fun addWork(work: Runnable, delayMilliseconds: Long) {
        checkContext()
        val newWork = SleepWork(work, SystemClock.elapsedRealtime() + delayMilliseconds)
        val index = works.add(newWork)
        if (index == 0)
            setupAlarm()
    }

    fun addWorkSeconds(work: Runnable, delaySeconds: Long) = addWork(work, delaySeconds * 1000L)
    fun addWorkMinutes(work: Runnable, delayMinutes: Long) = addWork(work, delayMinutes * 60000L)
    fun addWorkHours(work: Runnable, delayHours: Long) = addWork(work, delayHours * 60L * 60000L)

    fun cancelWork(work: Runnable) {
        val index = works.cancel(work)
        if (index == 0) {
            if (works.getFirst() != null)
                setupAlarm()
            else
                cancelAlarm()
        }
    }

    fun alarm() {
        val work = works.removeFirst()
        if (work != null) {
            work.work.run()
            if (works.getFirst() != null)
                setupAlarm()
        }
    }

    private fun checkContext() {
        if (context == null)
            context = AppContext.instance
    }

    private fun setupAlarm() {
        val work = works.getFirst()
        if (work != null) {
            val alarmIntent = Intent(context, javaClass<SleepWorkerBroadcast>())
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    work.elapsedTime,
                    pendingIntent)
            lastIntent = pendingIntent
        }
    }

    private fun cancelAlarm() {
        if (lastIntent != null) {
            val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(lastIntent)
            lastIntent = null
        }
    }

    private class SleepWork(
            val work: Runnable,
            val elapsedTime: Long
    )

    private class SleepCollection {
        private val elements = LinkedList<SleepWork>()

        fun add(work: SleepWork): Int {
            val elIndex = elements.indexOfFirst { it.elapsedTime > work.elapsedTime }
            val insertIndex = if (elIndex == -1) elements.count() else elIndex
            elements.add(insertIndex, work)
            return insertIndex
        }
        fun cancel(runnable: Runnable): Int {
            val index = elements.indexOfFirst { it.work == runnable }
            if (index != -1)
                elements.remove(index)
            return index
        }
        fun removeFirst() = if (elements.isNotEmpty()) elements.removeFirst() else null
        fun getFirst() = elements.firstOrNull()
    }

    public class SleepWorkerBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            SleepWorker.alarm()
        }
    }
}