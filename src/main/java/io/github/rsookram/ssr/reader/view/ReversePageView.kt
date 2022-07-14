package io.github.rsookram.ssr.reader.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.reader.Direction
import io.github.rsookram.ssr.reader.PageModeAdapter
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewState

class ReversePageView(context: Context) : RecyclerView(context), Reader {

    private val layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.HORIZONTAL,
        true
    ).apply { setLayoutManager(this) }

    private val adapter = PageModeAdapter().apply { setAdapter(this) }

    override var onPositionChanged: (Position) -> Unit = {}

    init {
        setBackgroundResource(android.R.color.black)

        doOnScrollStopped {
            val position = getPosition()
            if (position != null) {
                onPositionChanged(position)
            }
        }

        PagerSnapHelper().apply {
            attachToRecyclerView(this@ReversePageView)
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
        val currentIndex = layoutManager.findFirstVisibleItemPosition()
        if (currentIndex == NO_POSITION) {
            return
        }

        val index = when (direction) {
            Direction.LEFT -> currentIndex + 1
            Direction.RIGHT -> currentIndex - 1
            else -> currentIndex
        }.coerceIn(0, adapter.itemCount - 1)

        smoothScrollToPosition(index)
    }

    private fun moveTo(position: Position) {
        if (getPosition()?.isApproximatelyEqualTo(position) == true) {
            return
        }

        layoutManager.scrollToPositionWithOffset(position.pageIndex, 0)
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

        return Position(pageIndex = currentIndex, offset = 0.0)
    }
}
