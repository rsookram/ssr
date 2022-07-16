package io.github.rsookram.util

import android.view.View
import android.view.ViewGroup

fun ViewGroup.doOnNextLayout(listener: () -> Unit) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            listener()
            removeOnLayoutChangeListener(this)
        }
    })
}
