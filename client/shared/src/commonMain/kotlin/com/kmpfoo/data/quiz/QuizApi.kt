package com.kmpfoo.data.quiz

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class QuizApi(
    private val httpClient: HttpClient,
) {
    suspend fun fetchQuiz(baseUrl: String, theme: String): QuizPojo {
        return httpClient.get("${baseUrl}/quiz") {
            parameter("theme", theme)
        }.body()
    }
}

@Serializable
data class QuizPojo(
    val theme: String,
    val questions: List<QuestionPojo>
)

@Serializable
data class QuestionPojo(
    val question: String,
    val answers: List<AnswerPojo>,
)

@Serializable
data class AnswerPojo(
    val text: String,
    val explanation: String? = null,
    val correct: Boolean,
)
