package io.github.rsookram.ssr.reader.menu

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao
import io.github.rsookram.util.eventLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderMenuViewModel @Inject constructor(
    private val dao: BookDao,
    private val pageLoader: PageLoader,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val uri = savedStateHandle.get<Uri>(KEY_URI)!!

    private val _states = MutableStateFlow(ReaderMenuState())
    val states: Flow<ReaderMenuState> = _states

    private val _dismiss = eventLiveData<Unit>()
    val dismiss: LiveData<Unit> = _dismiss

    init {
        viewModelScope.launch {
            val pageCount = pageLoader.load(uri).size
            _states.value = _states.value.copy(pageCount = pageCount)
        }

        dao.books(uri)
            .onEach { book ->
                val currentValue = _states.value

                _states.value = if (currentValue.book == null) {
                    currentValue.copy(book = book)
                } else {
                    currentValue.copy(book = currentValue.book.copy(mode = book.mode))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onProgressChanged(progress: Int, isDragging: Boolean) {
        val book = _states.value.book ?: return
        val newBook = book.copy(position = book.position.copy(pageIndex = progress, offset = 0.0))

        _states.value = _states.value.copy(book = newBook)

        if (!isDragging) {
            dao.insert(newBook)
        }
    }

    fun onCropChanged(crop: Crop, isDragging: Boolean) {
        val book = _states.value.book ?: return
        val newBook = book.copy(crop = crop)

        _states.value = _states.value.copy(book = newBook)

        if (!isDragging) {
            dao.insert(newBook)
        }
    }

    fun onReadingModeSelected(mode: ReadingMode) {
        val book = _states.value.book ?: return
        val newBook = book.copy(mode = mode)

        _states.value = _states.value.copy(book = newBook)

        dao.insert(newBook)

        _dismiss.value = Unit
    }

    companion object {
        const val KEY_URI = "book_uri"
    }
}

data class ReaderMenuState(
    val book: Book? = null,
    val pageCount: Int? = null,
)
