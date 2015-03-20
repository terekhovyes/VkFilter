package me.alexeyterekhov.vkfilter.Common

import me.alexeyterekhov.vkfilter.R
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.properties.Delegates

public object DateFormat {
    private val SECONDS_IN_DAY = 3600 * 24
    private var initialized = false
    private var months: Array<String> by Delegates.notNull()
    private var today: String by Delegates.notNull()
    private var yesterday: String by Delegates.notNull()
    private var rightNow: String by Delegates.notNull()
    private var ago: String by Delegates.notNull()
    private var seconds: String by Delegates.notNull()
    private var minutes: String by Delegates.notNull()
    private var minutesFrom1To5: Array<String> by Delegates.notNull()
    private var hoursFrom1To6: Array<String> by Delegates.notNull()

    private fun init() {
        val context = AppContext.instance
        months = context.getResources()!!.getStringArray(R.array.months)
        today = context.getString(R.string.today)
        yesterday = context.getString(R.string.yesterday)
        rightNow = context.getString(R.string.right_now)
        ago = context.getString(R.string.ago)
        seconds = context.getString(R.string.seconds)
        minutes = context.getString(R.string.minutes)
        minutesFrom1To5 = context.getResources()!!.getStringArray(R.array.minutes1_5)
        hoursFrom1To6 = context.getResources()!!.getStringArray(R.array.hours1_6)
    }

    /*
    Util methods
     */
    private fun isToday(sec: Long): Boolean {
        val currentSeconds = System.currentTimeMillis() / 1000L
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000L)
        val now = Calendar.getInstance()
        return currentSeconds - sec < SECONDS_IN_DAY
                && cal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
    }

    private fun isYesterday(sec: Long): Boolean {
        val currentSeconds = System.currentTimeMillis() / 1000L
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000L)
        val ytd = Calendar.getInstance()
        ytd.add(Calendar.DATE, -1)
        return currentSeconds - sec < SECONDS_IN_DAY * 2
                && ytd.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisYear(sec: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000L)
        val cur = Calendar.getInstance()
        return cur.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    }

    private fun time(c: Calendar) = SimpleDateFormat("HH:mm").format(c.getTime())

    private fun dayMonth(c: Calendar)
            = SimpleDateFormat("dd ").format(c.getTime()) + months[c.get(Calendar.MONTH)]

    private fun monthYear(c: Calendar)
            = months[c.get(Calendar.MONTH)] + SimpleDateFormat("' '''yy").format(c.getTime())

    private fun dayMonthYear(c: Calendar)
            = dayMonth(c) + SimpleDateFormat("' '''yy").format(c.getTime())

    /*
    Shows:
    time of this day
    "yesterday" word
    day month
    month year
     */
    fun dialogReceivedDate(sec: Long): String {
        if (!initialized) init()
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000L)

        return when {
            isToday(sec) -> time(cal)
            isYesterday(sec) -> yesterday
            isThisYear(sec) -> dayMonth(cal)
            else -> monthYear(cal)
        }
    }

    /*
    Shows:
    "right now" phrase
    seconds ago
    minutes ago
    hours ago if < 7
     */
    fun lastUpdateTime(msc: Long): String {
        if (!initialized) init()
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(msc)
        val diffInSec = (System.currentTimeMillis() - msc) / 1000L
        val diffInMin = diffInSec / 60
        val diffInHours = diffInMin / 60

        return when {
            diffInSec < 10 -> rightNow
            diffInSec < 60 -> "${(diffInSec / 5) * 5} $seconds $ago"
            diffInMin < 10 -> "${minutesFrom1To5[if (diffInMin > 5) 4 else diffInMin.toInt() - 1]} $ago"
            diffInMin < 60 -> "${(diffInMin / 5) * 5} $minutes $ago"
            diffInHours < 7 -> "$diffInHours ${hoursFrom1To6[diffInHours.toInt() - 1]} $ago"
            isToday(msc / 1000L) -> time(cal)
            isYesterday(msc / 1000L) -> yesterday
            isThisYear(msc / 1000L) -> dayMonth(cal)
            else -> monthYear(cal)
        }
    }

    /*
    Shows:
    time
     */
    public fun time(sec: Long): String {
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000)
        return time(cal)
    }

    /*
    Shows:
    "today" word
    day month
    day month year
     */
    public fun messageListDayContainer(msc: Long): String {
        if (!initialized) init()
        val date = Calendar.getInstance()
        date.setTimeInMillis(msc)

        return when {
            isToday(msc / 1000L) -> today
            isThisYear(msc / 1000L) -> dayMonth(date)
            else -> dayMonthYear(date)
        }
    }

    /*
    Shows last seen time
     */
    public fun lastOnline(sec: Long): String {
        if (!initialized) init()
        val currentSeconds = System.currentTimeMillis() / 1000L
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(sec * 1000L)

        val diffInSec = currentSeconds - sec
        val diffInMin = diffInSec / 60
        val diffInHours = diffInMin / 60
        return when {
            diffInSec < 10 -> rightNow
            diffInSec < 60 -> "${(diffInSec / 5) * 5} $seconds $ago"
            diffInMin < 10 -> "${minutesFrom1To5[if (diffInMin > 5) 4 else diffInMin.toInt() - 1]} $ago"
            diffInMin < 60 -> "${(diffInMin / 5) * 5} $minutes $ago"
            diffInHours < 7 -> "$diffInHours ${hoursFrom1To6[diffInHours.toInt() - 1]} $ago"
            isToday(sec) -> "$today в ${time(sec)}"
            isYesterday(sec) -> "$yesterday в ${time(sec)}"
            isThisYear(sec) -> dayMonth(cal)
            else -> dayMonthYear(cal)
        }
    }
}