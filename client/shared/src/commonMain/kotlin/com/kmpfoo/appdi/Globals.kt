package com.kmpfoo.appdi


import co.early.fore.core.delegate.Fore
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object Globals {
    lateinit var app: Any
        private set

    fun init(
        application: Any
    ) {
        app = application
    }
}

suspend fun initialiseApp(
    isDebug: Boolean,
    application: Any
) {

    Fore.i("initialiseApp()")

    Globals.init(application)

    koinSetup(
        isDebuggable = isDebug,
        application = application,
    )
}

fun koinModules(): List<Module> = listOf(
    dataRest,
    domainModels,
    domainServices,
    core,
)

private fun koinSetup(isDebuggable: Boolean, application: Any) {
    startKoin {
        allowOverride(isDebuggable)
        modules(koinModules())
    }
}
