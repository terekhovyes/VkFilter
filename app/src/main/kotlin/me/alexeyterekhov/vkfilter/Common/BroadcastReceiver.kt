package me.alexeyterekhov.vkfilter.Common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent)
        = ReceiverStation.receiveIntent(intent)
}

object ReceiverStation {
    var lastNotReceivedIntent: Intent? = null
    var listener: IntentListener? = null
        set(l) {
            $listener = l
            if (l != null && lastNotReceivedIntent != null) {
                l.onGetIntent(lastNotReceivedIntent!!)
                lastNotReceivedIntent = null
            }
        }

    fun receiveIntent(intent: Intent) {
        if (listener == null)
            lastNotReceivedIntent = intent
        else
            listener!!.onGetIntent(intent)
    }
}

trait IntentListener {
    fun onGetIntent(intent: Intent);
}