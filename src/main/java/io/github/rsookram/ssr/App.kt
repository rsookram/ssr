package io.github.rsookram.ssr

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.rsookram.ssr.model.BookDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@HiltAndroidApp
class App : Application()

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun dao(@ApplicationContext context: Context) = BookDao.get(context)

    @Provides
    fun contentResolver(@ApplicationContext context: Context) = context.contentResolver

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
