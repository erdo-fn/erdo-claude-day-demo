package com.kmpfoo.appdi

import com.kmpfoo.data.quiz.QuizServiceImp
import com.kmpfoo.domain.service.quiz.QuizService
import org.koin.dsl.module

/**
 * Domain Services
 */
val domainServices = module {

    /**
     * Web Services
     */

    single<QuizService> {
        QuizServiceImp(
            api = get(),
            wrapper = get(),
        )
    }
}
