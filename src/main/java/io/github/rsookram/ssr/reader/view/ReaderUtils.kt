package io.github.rsookram.ssr.reader.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.ssr.entity.Position
import kotlin.math.abs

fun RecyclerView.doOnScrollStopped(action: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                action()
            }
        }
    })
}

fun Position.isApproximatelyEqualTo(other: Position) =
    pageIndex == other.pageIndex && abs(offset - other.offset) < 0.01

fun ViewGroup.waitForChildren(action: () -> Unit) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (childCount == 0) {
                return
            }

            view.removeOnLayoutChangeListener(this)

            // Perform action in a post to ensure it happens after the current
            // layout pass is complete. This allows action to request another
            // layout pass.
            post { action() }
        }
    })
}
