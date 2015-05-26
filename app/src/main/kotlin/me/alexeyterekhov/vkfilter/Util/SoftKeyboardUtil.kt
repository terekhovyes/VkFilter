package me.alexeyterekhov.vkfilter.Util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


public object SoftKeyboardUtil {
    fun show(editText: View) {
        val inputMan = getMan(editText)
        inputMan.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hide(editText: View) {
        val inputMan = getMan(editText)
        if (!inputMan.isActive())
            return
        inputMan.hideSoftInputFromWindow(editText.getWindowToken(), 0)
    }

    fun isShown(editText: View): Boolean {
        val inputMan = getMan(editText)
        return inputMan.isActive(editText)
    }

    private fun getMan(view: View) = view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}