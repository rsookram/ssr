package io.github.rsookram.ssr.entity

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "book"
)
data class Book(
    @PrimaryKey @ColumnInfo(name = "filename") val filename: String,
    @Embedded val position: Position,
    @ColumnInfo(name = "mode") val mode: ReadingMode,
    @Embedded val crop: Crop,
)

enum class ReadingMode {
    SCROLL_VERTICAL,
    SCROLL_REVERSE_HORIZONTAL,
    PAGE_REVERSE
}

@Entity
data class Position(
    @ColumnInfo(name = "page_index")
    @IntRange(from = 0)
    val pageIndex: Int,

    @ColumnInfo(name = "offset")
    @FloatRange(from = 0.0, to = 1.0, toInclusive = false)
    val offset: Double
)

@Entity
data class Crop(
    @ColumnInfo(name = "crop_left") val left: Int = 0,
    @ColumnInfo(name = "crop_top") val top: Int = 0,
    @ColumnInfo(name = "crop_right") val right: Int = 0,
    @ColumnInfo(name = "crop_bottom") val bottom: Int = 0,
)
