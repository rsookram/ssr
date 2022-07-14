package io.github.rsookram.util

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/** Helper class for detecting a double tap on a View */
class DoubleTapDetector(context: Context, onDoubleTapListener: () -> Unit) :
    GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
        }
    ),
    View.OnTouchListener {

    init {
        setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean = false

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTapListener()
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean = false
        })
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean =
        onTouchEvent(event)
}
