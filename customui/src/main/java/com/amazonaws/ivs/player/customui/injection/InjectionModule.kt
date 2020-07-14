package com.amazonaws.ivs.player.customui.injection

import androidx.room.Room
import com.amazonaws.ivs.player.customui.App
import com.amazonaws.ivs.player.customui.data.LocalCacheProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InjectionModule(private val context: App) {

    @Provides
    @Singleton
    fun provideLocalCacheProvider(): LocalCacheProvider
            = Room.databaseBuilder(context, LocalCacheProvider::class.java, "twitch_database").build()

}
