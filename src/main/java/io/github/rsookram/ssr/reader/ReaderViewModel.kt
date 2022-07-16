package io.github.rsookram.ssr.reader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao
import kotlinx.coroutines.flow.*

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val bookDao = BookDao.get(application)
    private val pageLoader = PageLoader(application.contentResolver)

    private val currentUri = MutableStateFlow<Uri?>(null)

    private val _states = MutableStateFlow(ReaderViewState(book = null, pages = null))
    val states: Flow<ReaderViewState> = _states

    var onShowMenu: (Uri) -> Unit = {}

    init {
        currentUri
            .filterNotNull()
            .onEach { uri ->
                bookDao.insertIfNotPresent(
                    Book(uri, Position(0, 0.0), ReadingMode.SCROLL_VERTICAL, Crop())
                )
            }
            .launchIn(viewModelScope)

        val books = currentUri
            .filterNotNull()
            .flatMapLatest(bookDao::books)

        val pageLists = currentUri
            .filterNotNull()
            .transformLatest { uri ->
                emit(emptyList())
                emit(pageLoader.load(uri))
            }

        combine(books, pageLists) { book, pages ->
            ReaderViewState(
                book,
                pages.map { CroppedPage(it, book.crop) },
            )
        }.onEach { state ->
            _states.value = state
        }.launchIn(viewModelScope)
    }

    fun loadBook(uri: Uri) {
        currentUri.value = uri
    }

    fun onPositionChanged(position: Position) {
        val book = _states.value.book ?: return

        bookDao.insert(book.copy(position = position))
    }

    fun onDoubleTap() {
        val uri = currentUri.value ?: return

        onShowMenu(uri)
    }
}

data class ReaderViewState(
    val book: Book?,
    val pages: List<CroppedPage>?,
)

