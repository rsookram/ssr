package io.github.rsookram.page

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.util.LruCache
import android.util.Size
import android.view.View
import android.widget.ImageView
import io.github.rsookram.util.MainExecutor
import okio.buffer
import okio.source
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream

class ImageLoader(private val contentResolver: ContentResolver) {

    private val loadingExecutor = Executors.newCachedThreadPool()
    private val bitmapCache: LruCache<CroppedPage, Bitmap> = LruCache(3)

    private val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    private var sizeCache: LruCache<Page, Size> = LruCache(64)

    fun loadPage(page: CroppedPage, view: ImageView) {
        (view.tag as? CompletableFuture<*>)?.cancel(false)

        val cached = bitmapCache.get(page)
        if (cached == null) {
            view.setImageBitmap(null)
        } else {
            view.setImageBitmap(cached)
            return
        }

        view.tag = view.waitUntilLaidOut()
            .thenApplyAsync({ loadBitmap(page, view.width, view.height) }, loadingExecutor)
            .thenAcceptAsync(
                { bitmap ->
                    bitmapCache.put(page, bitmap)
                    view.setImageBitmap(bitmap)
                },
                MainExecutor
            )
    }

    private fun View.waitUntilLaidOut(): CompletableFuture<Unit> {
        val completableFuture = CompletableFuture<Unit>()

        if (isLaidOut && !isLayoutRequested) {
            completableFuture.complete(Unit)
        } else {
            addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    completableFuture.complete(Unit)
                    removeOnLayoutChangeListener(this)
                }
            })
        }

        return completableFuture
    }

    private fun loadBitmap(page: CroppedPage, viewWidth: Int, viewHeight: Int): Bitmap {
        val buffer = ByteBuffer.wrap(page.page.getBytes())

        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(buffer)) { decoder, info, _ ->
            val imageWidth = info.size.width
            val imageHeight = info.size.height

            val (width, height) = if (viewWidth < imageWidth && viewHeight < imageHeight) {
                // Assume that the view and image have the same aspect ratio
                viewWidth to viewHeight
            } else {
                imageWidth to imageHeight
            }

            decoder.crop = Rect(
                page.crop.left,
                page.crop.top,
                width - page.crop.right,
                height - page.crop.bottom
            )

            decoder.setTargetSize(width, height)
        }
    }

    private fun Page.getBytes(byteCount: Long? = null): ByteArray {
        val stream = contentResolver.runCatching { openInputStream(uri) }.getOrNull()
            ?: return ByteArray(0)

        stream
            .apply { fullSkip(offset) }
            .source()
            .buffer()
            .inputStream()
            .let(::ZipInputStream)
            .use { zip ->
                zip.nextEntry

                zip.source().buffer().use {
                    return if (byteCount != null) {
                        it.readByteArray(byteCount)
                    } else {
                        it.readByteArray()
                    }
                }
            }
    }

    fun loadSize(page: Page): Size {
        val cached = sizeCache.get(page)
        if (cached != null) {
            return cached
        }

        val bytes = page.getBytes(640)

        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val size = Size(options.outWidth, options.outHeight)
        sizeCache.put(page, size)
        return size
    }

    companion object {

        private var instance: ImageLoader? = null

        fun get(context: Context): ImageLoader {
            val loader = instance ?: ImageLoader(context.contentResolver)
            instance = loader
            return loader
        }
    }
}
