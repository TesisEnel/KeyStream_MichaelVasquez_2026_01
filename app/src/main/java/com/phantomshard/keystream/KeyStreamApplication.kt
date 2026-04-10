package com.phantomshard.keystream

import android.app.Application
import com.phantomshard.keystream.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KeyStreamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@KeyStreamApplication)
            modules(appModule)
        }
    }
}
