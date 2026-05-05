package com.kmpfoo.appdi

import com.kmpfoo.domain.feature.quiz.QuizModel
import org.koin.dsl.module

/**
 * Domain Models (Features)
 */
val domainModels = module {

    single {
        QuizModel(
            quizService = get(),
            perSista = get(),
            logger = get(),
        )
    }
}
