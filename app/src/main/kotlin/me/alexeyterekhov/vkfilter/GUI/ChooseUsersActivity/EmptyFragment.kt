package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.TextView


public class EmptyFragment(): Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val tv = TextView(getActivity())
        tv setText "Text here!"
        return tv
    }
}