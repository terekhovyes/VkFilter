package me.alexeyterekhov.vkfilter.Internet.LongPoll

import android.util.Log
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestLongPollServer
import me.alexeyterekhov.vkfilter.Util.EventBuses

object LongPollControl {
    private val LOG_TAG = "LongPollControl"
    private var longPollLoop: LongPollLoop? = null
    var isRunning = false
        private set

    fun start() {
        isRunning = true
        if (longPollLoop != null) {
            Log.d(LOG_TAG, "Continue active long polling")
        } else {
            Log.d(LOG_TAG, "Start long polling")
            RequestControl.addBackground(RequestLongPollServer())
        }
    }

    fun stop() {
        isRunning = false
        Log.d(LOG_TAG, "Stop long polling")
    }

    fun configure(config: LongPollConfig) {
        longPollLoop = LongPollLoop(config.server, config.key)
        loop(config.ts)
    }

    fun loop(ts: String?) {
        if (isRunning) {
            if (ts == null) {
                Log.d(LOG_TAG, "Long polling broken, request new server")
                RequestControl.addBackground(RequestLongPollServer())
            } else {
                longPollLoop?.loopWhileRunning(ts)
            }
        } else {
            longPollLoop = null
        }
    }

    fun eventBus() = EventBuses.longPollBus()
}