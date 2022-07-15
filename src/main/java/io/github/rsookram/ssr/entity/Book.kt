package io.github.rsookram.ssr.entity

import android.net.Uri
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

data class Book(
    val uri: Uri,
    val position: Position,
    val mode: ReadingMode,
    val crop: Crop,
)

enum class ReadingMode {
    SCROLL_VERTICAL,
    SCROLL_REVERSE_HORIZONTAL,
    PAGE_REVERSE
}

data class Position(
    @IntRange(from = 0)
    val pageIndex: Int,

    @FloatRange(from = 0.0, to = 1.0, toInclusive = false)
    val offset: Double
)

data class Crop(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
)
