package com.kmpfoo.data.quiz

import co.early.fore.core.type.Either
import co.early.fore.net.wrap.Wrapper
import com.kmpfoo.data.DataError
import com.kmpfoo.data.toDomain
import com.kmpfoo.domain.DomainError
import com.kmpfoo.domain.service.quiz.Quiz
import com.kmpfoo.domain.service.quiz.QuizService

class QuizServiceImp(
    private val api: QuizApi,
    private val wrapper: Wrapper<DataError>,
) : QuizService {

    override suspend fun getQuiz(baseUrl: String, theme: String): Either<DomainError, Quiz> {
        return toDomain(wrapper.processCallAwait {
            api.fetchQuiz(baseUrl, theme)
        }) {
            it.toDomain()
        }
    }
}
