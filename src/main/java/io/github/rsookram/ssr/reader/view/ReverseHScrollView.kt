package io.github.rsookram.ssr.reader.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.ssr.R
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.reader.Direction
import io.github.rsookram.ssr.reader.PageAdapter
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewState
import io.github.rsookram.util.doOnNextLayout

class ReverseHScrollView(context: Context) : RecyclerView(context), Reader {

    private val layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.HORIZONTAL,
        true
    ).also(this::setLayoutManager)

    private val adapter = PageAdapter(ImageScaler.maxHeight)
        .also(this::setAdapter)

    private var pendingMove: Position? = null

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
            Direction.LEFT -> -distance
            Direction.RIGHT -> distance
            else -> 0
        }

        smoothScrollBy(offset, 0)
    }

    private fun moveTo(position: Position) {
        if (getPosition()?.isApproximatelyEqualTo(position) == true ||
            pendingMove?.isApproximatelyEqualTo(position) == true
        ) {
            return
        }

        layoutManager.scrollToPositionWithOffset(position.pageIndex, 0)
        if (position.offset == 0.0) {
            return
        }

        pendingMove = position

        doOnNextLayout {
            val newX = ((-getChildAt(0).width).toDouble() * position.offset).toInt()
            scrollBy(newX, 0)
            pendingMove = null
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
        val offset = (firstVisibleChild.right - right).toDouble() / firstVisibleChild.width
        return Position(currentIndex, offset)
    }
}
