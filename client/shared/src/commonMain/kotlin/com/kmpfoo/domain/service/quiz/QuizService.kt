package com.kmpfoo.domain.service.quiz

import co.early.fore.core.type.Either
import com.kmpfoo.domain.DomainError
import kotlinx.serialization.Serializable

interface QuizService {
    suspend fun getQuiz(baseUrl: String, theme: String): Either<DomainError, Quiz>
}

@Serializable
data class Quiz(
    val theme: String,
    val questions: List<Question> = listOf()
)

@Serializable
data class Question(
    val question: String,
    val answers: List<Answer>,
)

@Serializable
data class Answer(
    val answer: String,
    val explanation: String? = null,
    val correct: Boolean,
)
