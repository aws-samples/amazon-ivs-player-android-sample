package com.amazonaws.ivs.player.quizdemo

import android.app.Application
import com.amazonaws.ivs.player.quizdemo.injection.DaggerInjectionComponent
import com.amazonaws.ivs.player.quizdemo.injection.InjectionComponent
import com.amazonaws.ivs.player.quizdemo.injection.InjectionModule

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
