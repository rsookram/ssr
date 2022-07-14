package io.github.rsookram.ssr.reader

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
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val pageLoader: PageLoader,
) : ViewModel() {

    private val currentFile = MutableStateFlow<File?>(null)

    private val _states = MutableStateFlow(ReaderViewState(book = null, pages = null))
    val states: Flow<ReaderViewState> = _states

    private val _menuShows = eventLiveData<File>()
    val menuShows: LiveData<File> = _menuShows

    init {
        currentFile
            .filterNotNull()
            .onEach { file ->
                bookDao.insertIfNotPresent(
                    Book(file.name, Position(0, 0.0), ReadingMode.SCROLL_VERTICAL, Crop())
                )
            }
            .launchIn(viewModelScope)

        val books = currentFile
            .filterNotNull()
            .flatMapLatest { file ->
                bookDao.books(file.name)
            }

        val pageLists = currentFile
            .filterNotNull()
            .transformLatest { file ->
                emit(emptyList())
                emit(pageLoader.load(file))
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

    fun loadBook(file: File) {
        currentFile.value = file
    }

    fun onPositionChanged(position: Position) {
        val book = _states.value.book ?: return

        viewModelScope.launch {
            bookDao.insert(book.copy(position = position))
        }
    }

    fun onDoubleTap() {
        val uri = currentFile.value ?: return

        _menuShows.value = uri
    }
}

data class ReaderViewState(
    val book: Book?,
    val pages: List<CroppedPage>?,
)

