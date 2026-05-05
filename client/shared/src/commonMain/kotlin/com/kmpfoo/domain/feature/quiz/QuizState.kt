package com.kmpfoo.domain.feature.quiz

import com.kmpfoo.domain.DomainError
import com.kmpfoo.domain.service.quiz.Quiz
import kotlinx.serialization.*

@Serializable
data class QuizState(
    val baseUrl: String = "http://localhost:8000",
    val quiz: Quiz? = null,
    val responses: List<Response> = emptyList(),
    @Transient
    val loading: Boolean = false,
    @Transient
    val error: DomainError = DomainError.NoError,
) {
    fun score(): Int {
        return 0 // TODO
    }
}

@Serializable
sealed class Response {
    @Serializable
    data object NoResponse : Response()
    @Serializable
    data class Responded(val answerIndex: Int) : Response()
}
