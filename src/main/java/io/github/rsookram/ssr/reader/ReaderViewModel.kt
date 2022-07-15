package io.github.rsookram.ssr.reader

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.page.CroppedPage
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao
import io.github.rsookram.util.eventLiveData
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val pageLoader: PageLoader,
) : ViewModel() {

    private val currentUri = MutableStateFlow<Uri?>(null)

    private val _states = MutableStateFlow(ReaderViewState(book = null, pages = null))
    val states: Flow<ReaderViewState> = _states

    private val _menuShows = eventLiveData<Uri>()
    val menuShows: LiveData<Uri> = _menuShows

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

        _menuShows.value = uri
    }
}

data class ReaderViewState(
    val book: Book?,
    val pages: List<CroppedPage>?,
)

