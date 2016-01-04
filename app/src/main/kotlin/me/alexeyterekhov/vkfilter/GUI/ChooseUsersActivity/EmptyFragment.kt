package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


public class EmptyFragment(): Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val tv = TextView(activity)
        tv.text = "Text here!"
        return tv
    }
}