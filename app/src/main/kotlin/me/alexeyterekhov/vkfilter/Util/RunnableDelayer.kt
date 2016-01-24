package me.alexeyterekhov.vkfilter.Util

import android.os.Handler
import java.util.*

class RunnableDelayer {
    private val runnables = HashMap<String, Runnable>()
    private val handler = Handler()

    fun delay(id: String, runnable: Runnable, delayMillis: Long) {
        cancel(id)

        val runnableWrapper = executeRunnable(id, runnable)
        runnables.put(id, runnableWrapper)
        handler.postDelayed(runnableWrapper, delayMillis)
    }

    fun cancel(id: String) {
        if (runnables.containsKey(id)) {
            val runnable = runnables.remove(id)!!
            handler.removeCallbacks(runnable)
        }
    }

    private fun executeRunnable(id: String, work: Runnable) = Runnable {
        if (runnables.remove(id) != null)
            work.run()
    }
}