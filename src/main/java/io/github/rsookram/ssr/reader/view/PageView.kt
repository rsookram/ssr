package io.github.rsookram.ssr.reader.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.ImageLoader
import io.github.rsookram.ssr.entity.Crop

/**
 * Colour matrix that flips the components (`-1.0f * c + 255 = 255 - c`) and
 * keeps the alpha intact.
 *
 * From https://stackoverflow.com/questions/17841787/invert-colors-of-drawable/17871384#17871384
 */
private val NEGATIVE = floatArrayOf(
    -1.0f, 0f, 0f, 0f, 255f, // red
    0f, -1.0f, 0f, 0f, 255f, // green
    0f, 0f, -1.0f, 0f, 255f, // blue
    0f, 0f, 0f, 1.0f, 0f     // alpha
)

class PageView(context: Context, private val scaleImage: ScaleImage) : ImageView(context) {

    private val imageLoader = ImageLoader.get(context)

    private var imageWidth = 0
    private var imageHeight = 0
    private var crop = Crop()

    init {
        this.scaleType = ScaleType.FIT_XY
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun bind(page: CroppedPage) {
        val size = imageLoader.loadSize(page.page)
        val width = size.width
        val height = size.height

        if (imageWidth != width || imageHeight != height) {
            requestLayout()
        }
        imageWidth = width
        imageHeight = height

        if (page.crop != crop) {
            requestLayout()
        }
        crop = page.crop

        imageLoader.loadPage(page, this)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        // Invert colours in night mode
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            drawable?.colorFilter = ColorMatrixColorFilter(NEGATIVE)
        }

        super.setImageDrawable(drawable)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val (width, height) = scaleImage(
            imageWidth - crop.left - crop.right,
            imageHeight - crop.top - crop.bottom,
            widthSize,
            heightSize
        )

        setMeasuredDimension(width, height)
    }
}
