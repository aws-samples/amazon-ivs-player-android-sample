package com.amazonaws.ivs.player.customui

import android.app.Application
import com.amazonaws.ivs.player.customui.injection.DaggerInjectionComponent
import com.amazonaws.ivs.player.customui.injection.InjectionComponent
import com.amazonaws.ivs.player.customui.injection.InjectionModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        component = DaggerInjectionComponent.builder().injectionModule(InjectionModule(this)).build()
    }

    companion object {
        lateinit var component: InjectionComponent
            private set
    }
}
