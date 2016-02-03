package me.alexeyterekhov.vkfilter.GUI.Common

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardUtils {
    fun hideSoftKeyboard(activity: Activity?) {
        if (activity == null)
            return
        hideSoftKeyboard(activity, false, activity.window.decorView)
    }

    fun hideSoftKeyboard(context: Context?, clearFocus: Boolean, vararg views: View) {
        if (null == context)
            return
        val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        for (currentView in views) {
            if (clearFocus)
                currentView.clearFocus()
            manager.hideSoftInputFromWindow(currentView.windowToken, 0)
            manager.hideSoftInputFromWindow(currentView.applicationWindowToken, 0)
        }
    }
}