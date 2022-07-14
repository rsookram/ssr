package io.github.rsookram.ssr.reader.menu

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import io.github.rsookram.ssr.R
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.util.SimpleSeekBarListener

class ReaderMenuView(
    context: Context,
    private val vm: ReaderMenuViewModel
) : FrameLayout(context) {

    private val bookProgress by lazy { findViewById<SeekBar>(R.id.book_progress) }
    private val currentPageLabel by lazy { findViewById<TextView>(R.id.current_page) }
    private val pageCountLabel by lazy { findViewById<TextView>(R.id.total_page_count) }
    private val modeGroup by lazy { findViewById<RadioGroup>(R.id.reading_mode_group) }

    private val cropLeft by lazy { findViewById<SeekBar>(R.id.crop_left) }
    private val cropTop by lazy { findViewById<SeekBar>(R.id.crop_top) }
    private val cropRight by lazy { findViewById<SeekBar>(R.id.crop_right) }
    private val cropBottom by lazy { findViewById<SeekBar>(R.id.crop_bottom) }

    init {
        id = VIEW_ID

        View.inflate(context, R.layout.view_reader_menu, this)

        setupPosition()
        setupReadingMode()
        setupCrop()
    }

    private fun setupPosition() {
        bookProgress.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                vm.onProgressChanged(progress, true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                vm.onProgressChanged(seekBar.progress, false)
            }
        })
    }

    private fun setupReadingMode() {
        var lastId = -1
        var isFirst = true
        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == lastId) {
                return@setOnCheckedChangeListener
            }

            lastId = checkedId
            if (isFirst) {
                isFirst = false
                return@setOnCheckedChangeListener
            }

            val mode = when (checkedId) {
                R.id.vertical_scroll -> ReadingMode.SCROLL_VERTICAL
                R.id.horizontal_scroll_reverse -> ReadingMode.SCROLL_REVERSE_HORIZONTAL
                R.id.page_reverse -> ReadingMode.PAGE_REVERSE
                else -> null
            }

            if (mode != null) {
                vm.onReadingModeSelected(mode)
            }
        }
    }

    private fun setupCrop() {
        setupCropSeekBar(cropLeft) { copy(left = it) }
        setupCropSeekBar(cropTop) { copy(top = it) }
        setupCropSeekBar(cropRight) { copy(right = it) }
        setupCropSeekBar(cropBottom) { copy(bottom = it) }
    }

    private fun setupCropSeekBar(view: SeekBar, update: Crop.(Int) -> Crop) {
        view.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                vm.onCropChanged(currentCrop().update(progress), true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                vm.onCropChanged(currentCrop().update(seekBar.progress), false)
            }
        })
    }

    private fun currentCrop() =
        Crop(cropLeft.progress, cropTop.progress, cropRight.progress, cropBottom.progress)

    fun bind(state: ReaderMenuState) {
        val (book, pageCount) = state
        book ?: return
        pageCount ?: return

        bookProgress.max = pageCount - 1
        bookProgress.progress = book.position.pageIndex

        currentPageLabel.text = (book.position.pageIndex + 1).toString()
        pageCountLabel.text = pageCount.toString()

        modeGroup.check(
            when (book.mode) {
                ReadingMode.SCROLL_VERTICAL -> R.id.vertical_scroll
                ReadingMode.SCROLL_REVERSE_HORIZONTAL -> R.id.horizontal_scroll_reverse
                ReadingMode.PAGE_REVERSE -> R.id.page_reverse
            }
        )

        cropLeft.progress = book.crop.left
        cropTop.progress = book.crop.top
        cropRight.progress = book.crop.right
        cropBottom.progress = book.crop.bottom
    }

    companion object {

        private val VIEW_ID = View.generateViewId()
    }
}
