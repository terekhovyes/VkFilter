package me.alexeyterekhov.vkfilter.Data.Entities.Message

class SentState : Cloneable {
    var state = State.STATE_IN_EDIT
    var id = 0L
    var timeMillis = 0L

    public enum class State {
        STATE_SENT,
        STATE_SENDING,
        STATE_IN_EDIT
    }

    override public fun clone(): Any {
        val copy = SentState()

        copy.state = state
        copy.id = id
        copy.timeMillis = timeMillis

        return copy
    }
}