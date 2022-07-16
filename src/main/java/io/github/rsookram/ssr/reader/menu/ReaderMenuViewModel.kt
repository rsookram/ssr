package io.github.rsookram.ssr.reader.menu

import android.content.Context
import android.net.Uri
import io.github.rsookram.page.PageLoader
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.model.BookDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReaderMenuViewModel(context: Context, private val scope: CoroutineScope) {

    private val dao = BookDao.get(context)
    private val pageLoader = PageLoader(context.contentResolver)

    private val _states = MutableStateFlow(ReaderMenuState())
    val states: Flow<ReaderMenuState> = _states

    var onDismiss: () -> Unit = {}

    fun setUri(uri: Uri) {
        scope.launch {
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
            .launchIn(scope)
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

        onDismiss()
    }
}

data class ReaderMenuState(
    val book: Book? = null,
    val pageCount: Int? = null,
)
