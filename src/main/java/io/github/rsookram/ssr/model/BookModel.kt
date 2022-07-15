package io.github.rsookram.ssr.model

import android.content.Context
import android.net.Uri
import io.github.rsookram.ssr.entity.Book
import io.github.rsookram.ssr.entity.Crop
import io.github.rsookram.ssr.entity.Position
import io.github.rsookram.ssr.entity.ReadingMode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.json.JSONObject

class BookDao(context: Context) {

    private val prefs = context.getSharedPreferences("book", Context.MODE_PRIVATE)

    private val books = MutableSharedFlow<Book>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = Int.MAX_VALUE,
    )

    fun insertIfNotPresent(book: Book) {
        if (prefs.getString(key(book), null) == null) {
            insert(book)
        }
    }

    fun insert(book: Book) {
        prefs.edit()
            .putString(key(book), toJson(book))
            .apply()

        books.tryEmit(book)
    }

    fun books(uri: Uri) = flow<Book> {
        val book = get(uri)
        if (book != null) {
            emit(book)
        }

        emitAll(books.filter { it.uri == uri })
    }

    private fun get(uri: Uri): Book? {
        val json = prefs.getString(uri.toString(), null) ?: return null
        return fromJson(uri, json)
    }

    private fun key(book: Book): String = book.uri.toString()

    private fun toJson(book: Book): String {
        return JSONObject().apply {
            put("pageIndex", book.position.pageIndex)
            put("offset", book.position.offset)
            put("mode", book.mode.name)
            put("leftCrop", book.crop.left)
            put("topCrop", book.crop.top)
            put("rightCrop", book.crop.right)
            put("bottomCrop", book.crop.bottom)
        }.toString()
    }

    private fun fromJson(uri: Uri, jsonStr: String): Book? {
        val json = kotlin.runCatching { JSONObject(jsonStr) }.getOrNull() ?: return null

        return Book(
            uri,
            Position(
                json.runCatching { getInt("pageIndex") }.getOrNull() ?: return null,
                json.runCatching { getDouble("offset") }.getOrNull() ?: return null,
            ),
            json.runCatching { getString("mode") }.mapCatching(ReadingMode::valueOf).getOrNull()
                ?: return null,
            Crop(
                json.runCatching { getInt("leftCrop") }.getOrNull() ?: return null,
                json.runCatching { getInt("topCrop") }.getOrNull() ?: return null,
                json.runCatching { getInt("rightCrop") }.getOrNull() ?: return null,
                json.runCatching { getInt("bottomCrop") }.getOrNull() ?: return null,
            )
        )
    }

    companion object {

        private var instance: BookDao? = null

        fun get(context: Context): BookDao {
            val dao = instance ?: BookDao(context)
            instance = dao
            return dao
        }
    }
}
