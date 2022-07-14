package io.github.rsookram.ssr.reader

import android.content.Context
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.reader.view.ReverseHScrollView
import io.github.rsookram.ssr.reader.view.ReversePageView
import io.github.rsookram.ssr.reader.view.VerticalScrollView

interface Reader {

    class Factory(private val context: Context) {

        fun create(mode: ReadingMode): Reader =
            when (mode) {
                ReadingMode.SCROLL_VERTICAL -> VerticalScrollView(context)
                ReadingMode.SCROLL_REVERSE_HORIZONTAL -> ReverseHScrollView(context)
                ReadingMode.PAGE_REVERSE -> ReversePageView(context)
            }.apply {
                defaultFocusHighlightEnabled = false
            }
    }

    fun bind(state: ReaderViewState)

    fun moveTo(direction: Direction)

    var onPositionChanged: (Position) -> Unit
}

enum class Direction {
    LEFT, UP, RIGHT, DOWN
}
