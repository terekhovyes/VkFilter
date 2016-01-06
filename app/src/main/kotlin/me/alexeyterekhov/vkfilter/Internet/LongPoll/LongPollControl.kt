package me.alexeyterekhov.vkfilter.Internet.LongPoll

import android.util.Log
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestLongPollServer
import me.alexeyterekhov.vkfilter.Util.EventBuses

object LongPollControl {
    private val LOG_TAG = "LongPollControl"
    var longPollLoop: LongPollLoop? = null
    var isRunning = false
        private set


    fun start() {
        Log.d(LOG_TAG, "Start long polling")
        isRunning = true
        RequestControl.addBackground(RequestLongPollServer())
    }

    fun stop() {
        Log.d(LOG_TAG, "Stop long polling")
        isRunning = false
        longPollLoop = null
    }

    fun configure(config: LongPollConfig) {
        if (isRunning) {
            longPollLoop = LongPollLoop(config.server, config.key)
            longPollLoop!!.loopWhileRunning(config.ts)
        }
    }

    fun loop(ts: String) {
        if (isRunning)
            longPollLoop?.loopWhileRunning(ts)
    }

    fun eventBus() = EventBuses.longPollBus()
}