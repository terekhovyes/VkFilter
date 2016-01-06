package me.alexeyterekhov.vkfilter.Internet.LongPoll

import me.alexeyterekhov.vkfilter.Util.Chef

class LongPollLoop(val server: String, val key: String) {
    private val WAIT_SECONDS = 25
    private val REQUEST_MODE = 2

    fun loopWhileRunning(ts: String) {
        if (!LongPollControl.isRunning)
            return

        Chef.cook(LongPollRecipe.recipe, buildUrl(ts))
    }

    private fun buildUrl(ts: String): String {
        return "http://$server?act=a_check&key=$key&ts=$ts&wait=$WAIT_SECONDS&mode=$REQUEST_MODE"
    }
}