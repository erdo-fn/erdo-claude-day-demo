package com.kmpfoo.appdi

import co.early.fore.core.delegate.DelegateDebug
import co.early.fore.core.delegate.Fore
import com.kmpfoo.domain.feature.quiz.QuizModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoinIos() {
    Fore.setDelegate(DelegateDebug(tagPrefix = "c.day"))
    Globals.init(application = Unit) // native filesPath ignores this; prevents lateinit crash
    startKoin {
        allowOverride(false)
        modules(koinModules())
    }
}

fun getQuizModel(): QuizModel = KoinPlatform.getKoin().get(QuizModel::class)
