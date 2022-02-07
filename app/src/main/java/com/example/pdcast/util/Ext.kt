package com.example.pdcast.util

import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard() }
}

fun View.visible(animate: Boolean = false, animationDuration: Long = 300) {
    if (animate) {
        this.startAnimation(AlphaAnimation(0F, 1F).apply {
            duration = animationDuration
            fillAfter = true
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        })
    } else {
        visibility = View.VISIBLE
    }
}

fun View.gone(animate: Boolean = false, animationDuration: Long = 300) {
    if (animate) {
        this.startAnimation(AlphaAnimation(1F, 0F).apply {
            duration = animationDuration
            fillAfter = true
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        })
    } else {
        visibility = View.GONE
    }
}