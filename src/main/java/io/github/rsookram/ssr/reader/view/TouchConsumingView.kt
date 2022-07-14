package io.github.rsookram.ssr.reader.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View

class TouchConsumingView(context: Context) : View(context) {

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = true
}
