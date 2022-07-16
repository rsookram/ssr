package io.github.rsookram.ssr.reader.menu

import android.content.Context
import android.net.Uri
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao
import io.github.rsookram.util.MainExecutor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class ReaderMenuViewModel(context: Context, private val uri: Uri) {

    private val dao = BookDao.get(context)
    private val pageLoader = PageLoader(context.contentResolver)

    private val cancellables = mutableListOf<Future<*>>()

    private var state = ReaderMenuState()
        set(value) {
            field = value
            onState(value)
        }

    var onState: (ReaderMenuState) -> Unit = {}
    var onDismiss: () -> Unit = {}

    private val onBookUpdate: (Book) -> Unit = { book ->
        if (book.uri == uri) {
            val currentValue = state

            state = if (currentValue.book == null) {
                currentValue.copy(book = book)
            } else {
                currentValue.copy(book = currentValue.book.copy(mode = book.mode))
            }
        }
    }

    init {
        cancellables += CompletableFuture
            .supplyAsync { pageLoader.load(uri).size }
            .thenAcceptAsync(
                { pageCount ->
                    state = state.copy(pageCount = pageCount)
                },
                MainExecutor
            )

        val book = dao.get(uri)
        if (book != null) {
            onBookUpdate(book)
        }

        dao.addBookUpdateListener(onBookUpdate)
    }

    fun onProgressChanged(progress: Int, isDragging: Boolean) {
        val book = state.book ?: return
        val newBook = book.copy(position = book.position.copy(pageIndex = progress, offset = 0.0))

        state = state.copy(book = newBook)

        if (!isDragging) {
            dao.insert(newBook)
        }
    }

    fun onCropChanged(crop: Crop, isDragging: Boolean) {
        val book = state.book ?: return
        val newBook = book.copy(crop = crop)

        state = state.copy(book = newBook)

        if (!isDragging) {
            dao.insert(newBook)
        }
    }

    fun onReadingModeSelected(mode: ReadingMode) {
        val book = state.book ?: return
        val newBook = book.copy(mode = mode)

        state = state.copy(book = newBook)

        dao.insert(newBook)

        onDismiss()
    }

    fun onCleared() {
        dao.removeBookUpdateListener(onBookUpdate)
        cancellables.forEach { it.cancel(false) }
        cancellables.clear()
    }
}

data class ReaderMenuState(
    val book: Book? = null,
    val pageCount: Int? = null,
)
