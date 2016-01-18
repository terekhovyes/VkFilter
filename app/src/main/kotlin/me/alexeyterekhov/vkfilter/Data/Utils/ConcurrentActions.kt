package me.alexeyterekhov.vkfilter.Data.Utils

import android.os.Handler
import java.util.*

class ConcurrentActions(
        val delayMillis: Long,
        val waitMillis: Long
) {
    /*
    Object wait some time before calling action 1 and:
        1)  if action 2 called while action 1 was waiting
            object cancel action 1
            object execute action 2 immediately
        2)  if action 1 was called
            object execute action 2 in >= (delayMillis) after action 1
     */
    private val handler = Handler()
    private val firstActions = HashMap<Long, Runnable>()
    private val executionTime = HashMap<Long, Long>()

    fun firstAction(concurrentId: Long, action: () -> Unit) {
        val runnable = Runnable({
            executionTime[concurrentId] = System.currentTimeMillis()
            firstActions.remove(concurrentId)
            action()
        })
        firstActions.put(concurrentId, runnable)
        handler.postDelayed(runnable, waitMillis)
    }

    fun secondAction(
            concurrentId: Long,
            doIfFirstActionWaiting: () -> Unit,
            doIfFirstActionCalled: () -> Unit
    ) {
        if (firstActions.contains(concurrentId)) {
            handler.removeCallbacks(firstActions[concurrentId])
            doIfFirstActionWaiting()
        } else {
            val time = System.currentTimeMillis()
            val exec = executionTime.remove(concurrentId)!!
            if (time - exec > delayMillis)
                doIfFirstActionCalled()
            else
                handler.postDelayed(doIfFirstActionCalled, exec + delayMillis - time)
        }
    }
}