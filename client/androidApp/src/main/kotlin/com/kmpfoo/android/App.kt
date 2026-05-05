package com.kmpfoo.android

import android.app.Application
import co.early.fore.core.coroutine.launchMainImm
import co.early.fore.core.delegate.DelegateDebug
import co.early.fore.core.delegate.Fore
import com.kmpfoo.appdi.initialiseApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Fore.setDelegate(DelegateDebug(
            tagPrefix = "c.day"
        ))

        launchMainImm {
            initialiseApp(
                isDebug = true,
                application = this@App
            )
        }
    }
}
