package io.github.rsookram.ssr.reader

import android.content.Context
import android.net.Uri
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao

class ReaderViewModel(applicationContext: Context) {

    private val bookDao = BookDao.get(applicationContext)
    private val pageLoader = PageLoader(applicationContext.contentResolver)

    private var currentUri: Uri? = null

    private var state = ReaderViewState(book = null, pages = null)
        set(value) {
            field = value
            onState(value)
        }

    var onState: (ReaderViewState) -> Unit = {}
        set(value) {
            if (state.book != null) {
                value(state)
            }
            field = value
        }
    var onShowMenu: (Uri) -> Unit = {}

    private val onBookUpdate: (Book) -> Unit = { book ->
        if (book.uri == currentUri) {
            state = state.copy(book = book)
        }
    }

    fun loadBook(uri: Uri) {
        if (uri == currentUri) {
            return
        }

        currentUri = uri

        val defaultBook = Book(uri, Position(0, 0.0), ReadingMode.SCROLL_VERTICAL, Crop())
        bookDao.insertIfNotPresent(defaultBook)

        val book = bookDao.get(uri) ?: defaultBook
        state = ReaderViewState(
            book = book,
            // TODO: Move page loading to a background thread
            pages = pageLoader.load(uri).map { CroppedPage(it, book.crop) },
        )

        bookDao.addBookUpdateListener(onBookUpdate)
    }

    fun onPositionChanged(position: Position) {
        val book = state.book ?: return

        bookDao.insert(book.copy(position = position))
    }

    fun onDoubleTap() {
        val uri = currentUri ?: return

        onShowMenu(uri)
    }

    fun onCleared() {
        bookDao.removeBookUpdateListener(onBookUpdate)
    }
}

data class ReaderViewState(
    val book: Book?,
    val pages: List<CroppedPage>?,
)

