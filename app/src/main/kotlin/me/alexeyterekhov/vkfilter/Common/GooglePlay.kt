package me.alexeyterekhov.vkfilter.Common

import android.app.Activity
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.ConnectionResult
import android.util.Log

object GooglePlay {
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    public fun checkGooglePlayServices(activity: Activity): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
            } else {
                Log.d("Play services", "This device is not supported.")
            }
            return false
        }
        return true
    }
}