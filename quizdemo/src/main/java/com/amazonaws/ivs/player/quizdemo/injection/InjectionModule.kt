package com.amazonaws.ivs.player.quizdemo.injection

import androidx.room.Room
import com.amazonaws.ivs.player.quizdemo.App
import com.amazonaws.ivs.player.quizdemo.data.LocalCacheProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InjectionModule(private val context: App) {

    @Provides
    @Singleton
    fun provideLocalCacheProvider(): LocalCacheProvider =
        Room.databaseBuilder(context, LocalCacheProvider::class.java, "quiz_database").build()

}
