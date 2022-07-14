package io.github.rsookram.ssr.reader.view

import android.content.Context
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.ssr.R
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.reader.Direction
import io.github.rsookram.ssr.reader.PageAdapter
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewState

class VerticalScrollView(context: Context) : RecyclerView(context), Reader {

    private val layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.VERTICAL,
        false
    ).also(this::setLayoutManager)

    private val adapter = PageAdapter(ImageScaler.maxWidth)
        .also(this::setAdapter)

    override var onPositionChanged: (Position) -> Unit = {}

    init {
        setBackgroundResource(R.color.bg_scroll_mode)

        doOnScrollStopped {
            val position = getPosition()
            if (position != null) {
                onPositionChanged(position)
            }
        }
    }

    override fun bind(state: ReaderViewState) {
        adapter.submitList(state.pages) {
            if (state.book?.position != null) {
                if (childCount == 0) {
                    waitForChildren { moveTo(state.book.position) }
                } else {
                    moveTo(state.book.position)
                }
            }
        }
    }

    override fun moveTo(direction: Direction) {
        val distance = resources.getDimensionPixelSize(R.dimen.arrow_key_scroll_distance)

        val offset = when (direction) {
            Direction.UP -> -distance
            Direction.DOWN -> distance
            else -> 0
        }

        smoothScrollBy(0, offset)
    }

    private fun moveTo(position: Position) {
        if (getPosition()?.isApproximatelyEqualTo(position) == true) {
            return
        }

        layoutManager.scrollToPositionWithOffset(position.pageIndex, 0)
        if (position.offset == 0.0) {
            return
        }

        doOnNextLayout {
            val newY = (getChildAt(0).height * position.offset).toInt()
            scrollBy(0, newY)
        }
    }

    private fun getPosition(): Position? {
        // This can happen if #onBookLoaded hasn't been called yet
        if (childCount == 0) {
            return null
        }

        val currentIndex = layoutManager.findFirstVisibleItemPosition()
        if (currentIndex == NO_POSITION) {
            return null
        }

        val firstVisibleChild = getChildAt(0)
        val offset = (-firstVisibleChild.top).toDouble() / firstVisibleChild.height
        return Position(currentIndex, offset)
    }
}
