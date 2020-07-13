package com.amazonaws.ivs.player.customui.injection

import com.amazonaws.ivs.player.customui.activities.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [InjectionModule::class])
interface InjectionComponent {
    fun inject(target: MainActivity)
}
