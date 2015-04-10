package me.alexeyterekhov.vkfilter.NotificationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


public class TestBroadcast: BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        Log.d("TestBroadcast", "Receive intent")
    }
}