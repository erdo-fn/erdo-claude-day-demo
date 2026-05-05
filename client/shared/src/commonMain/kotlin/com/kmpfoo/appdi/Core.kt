package com.kmpfoo.appdi

import co.early.fore.core.delegate.Fore
import co.early.fore.core.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val core = module {

    single<CoroutineDispatcher>(qualifier = named(DispatchersQualifiers.Default)) {
        Dispatchers.Default
    }
    single<CoroutineDispatcher>(qualifier = named(DispatchersQualifiers.IO)) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(qualifier = named(DispatchersQualifiers.Main)) {
        Dispatchers.Main
    }

    single<Logger> {
        Fore.getLogger()
    }

}

object DispatchersQualifiers {
    const val IO = "IO"
    const val Default = "Default"
    const val Main = "Main"
}