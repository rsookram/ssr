package io.github.rsookram.ssr.reader.view

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewModel
import io.github.rsookram.util.DoubleTapDetector

class MainView(
    private val container: FrameLayout,
    private val vm: ReaderViewModel
) {

    fun createReader(mode: ReadingMode): Reader {
        val context = container.context

        val reader = Reader.Factory(context).create(mode)
        val contentView = reader as View

        container.removeAllViews()
        container.addView(contentView)

        val ignoreBottomHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics
        )
        container.addView(
            TouchConsumingView(container.context),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ignoreBottomHeight.toInt(),
                Gravity.BOTTOM
            )
        )

        reader.onPositionChanged = vm::onPositionChanged

        contentView.setOnTouchListener(
            DoubleTapDetector(context, vm::onDoubleTap)
        )

        return reader
    }
}
