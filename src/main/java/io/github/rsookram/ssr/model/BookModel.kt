package io.github.rsookram.ssr.model

import androidx.room.*
import io.github.rsookram.ssr.entity.Book
import kotlinx.coroutines.flow.Flow

@Database(entities = [Book::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
}

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotPresent(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Query("SELECT * FROM book WHERE filename = :filename")
    fun books(filename: String): Flow<Book>
}
