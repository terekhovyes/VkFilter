package me.alexeyterekhov.vkfilter.Internet

import java.util.*

object RequestFrequencyControl {
    private val REQUESTS_PER_PERIOD = 3
    private val PERIOD_MILLIS = 1000

    private val lastRequestTimes = LinkedList<Long>()

    fun waitNext() {
        var waitTime = 0L

        synchronized(lastRequestTimes, {
            if (lastRequestTimes.count() < REQUESTS_PER_PERIOD) {
                putCurrentTime()
            } else {
                waitTime = putAndCalculateTimeForWaiting()
            }
        })

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime)
            } catch (e: InterruptedException) {}
        }
    }

    private fun putAndCalculateTimeForWaiting(): Long {
        val oldestRequestTime = lastRequestTimes.removeFirst()
        val currentTime = time()
        val waitTime = if (currentTime - oldestRequestTime > PERIOD_MILLIS)
            0L
        else
            PERIOD_MILLIS + oldestRequestTime - currentTime
        lastRequestTimes.add(currentTime + waitTime)
        return waitTime
    }

    private fun putCurrentTime() {
        lastRequestTimes.add(time())
    }

    private fun time() = System.currentTimeMillis()
}