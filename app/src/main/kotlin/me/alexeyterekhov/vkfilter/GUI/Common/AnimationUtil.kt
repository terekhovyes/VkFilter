package me.alexeyterekhov.vkfilter.GUI.Common

import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation

object AnimationUtil {
    fun typingAnimationWhileVisible(view: View) {
        if (view.visibility != View.VISIBLE)
            return

        view.clearAnimation()

        val translation = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.25f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f)
        translation.fillAfter = true
        translation.isFillEnabled = true
        translation.duration = 1000
        translation.interpolator = LinearInterpolator()

        val returning = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0.25f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f)
        returning.duration = 100

        translation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                Handler().postDelayed({
                    if (view.visibility == View.VISIBLE)
                        view.startAnimation(returning)
                }, 500)
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })

        returning.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                typingAnimationWhileVisible(view)
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })

        view.startAnimation(translation)
    }
}