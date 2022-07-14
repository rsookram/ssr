package io.github.rsookram.ssr

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.rsookram.ssr.model.AppDatabase
import io.github.rsookram.ssr.model.BookDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun dao(@ApplicationContext context: Context): BookDao =
        Room.databaseBuilder(context, AppDatabase::class.java, "book.db")
            .build()
            .bookDao()

    @UiDispatcher
    @Provides
    fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @BgDispatcher
    @Provides
    fun bgDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UiDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BgDispatcher
