package com.kmpfoo.domain.feature.quiz

import co.early.fore.core.coroutine.launchIO
import co.early.fore.core.logging.Logger
import co.early.fore.core.observer.Observable
import co.early.fore.core.observer.ObservableImp
import co.early.fore.core.type.Either
import co.early.persista.PerSista
import com.kmpfoo.domain.DomainError
import com.kmpfoo.domain.service.quiz.Quiz
import com.kmpfoo.domain.service.quiz.QuizService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class QuizModel(
    private val quizService: QuizService,
    private val perSista: PerSista,
    private val logger: Logger,
) : Observable by ObservableImp() {

    var state = QuizState(loading = true)
        private set

    init {
        launchIO {
            delay(1000.milliseconds) // not necessary, it just lets us verify the initial loading spinner
            perSista.read(state) {
                state = it.copy(loading = false)
                notifyObservers()
            }
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        if (!state.loading) {
            perSista.write(state.copy(baseUrl = baseUrl)) {
                logger.i("updateBaseUrl() updated:${it.baseUrl}")
                state = it
                notifyObservers()
            }
        }
    }

    fun updateQuizTheme(quizTheme: String) {
        if (!state.loading) {
            perSista.write(
                item = state.copy(
                    quiz = Quiz(
                        theme = quizTheme,
                    ),
                    responses = emptyList()
                )
            ) {
                logger.i("updateQuizTheme() updated:${it.quiz?.theme}")
                state = it
                notifyObservers()
            }
        }
    }

    fun fetchQuiz() {

        val quizTheme = state.quiz?.theme

        if (!state.loading && quizTheme != null) {

            state = state.copy(
                loading = true,
                error = DomainError.NoError
            )
            notifyObservers()

            launchIO {

                val response = quizService.getQuiz(
                    baseUrl = state.baseUrl,
                    theme = quizTheme,
                )

                when (response) {
                    is Either.Fail<DomainError> -> {
                        perSista.write(
                            item = state.copy(
                                loading = false,
                                error = response.value
                            )
                        ) {
                            logger.i("fetchQuiz() error: ${response.value}")
                            state = it
                            notifyObservers()
                        }
                    }

                    is Either.Success<Quiz> -> {
                        perSista.write(
                            item = QuizState(
                                baseUrl = state.baseUrl,
                                loading = false,
                                quiz = response.value,
                                error = DomainError.NoError,
                                responses = response.value.questions.map {
                                    Response.NoResponse
                                }.toList()
                            )
                        ) {
                            logger.i("fetchQuiz() success ${response.value.theme}")
                            state = it
                            notifyObservers()
                        }
                    }
                }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        if (!state.loading) {
            perSista.write(
                item = state.copy(
                    responses = state.responses.mapIndexed { index, response ->
                        if (index == questionIndex) {
                            Response.Responded(answerIndex)
                        } else response
                    }
                )
            ) {
                logger.i("selectAnswer() question:$questionIndex answer:$answerIndex")
                state = it
                notifyObservers()
            }
        }
    }

    fun resetResponses() {
        perSista.write(
            item = state.copy(
                responses = state.quiz?.questions?.map {
                    Response.NoResponse
                }?.toList() ?: emptyList()
            )
        ) {
            logger.i("resetResponses()")
            state = it
            notifyObservers()
        }
    }
}
