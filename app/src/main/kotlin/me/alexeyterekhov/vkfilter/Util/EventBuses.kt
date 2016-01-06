package me.alexeyterekhov.vkfilter.Util

import de.greenrobot.event.EventBus

object EventBuses {
    fun longPollBus() = EventBus.getDefault()
}