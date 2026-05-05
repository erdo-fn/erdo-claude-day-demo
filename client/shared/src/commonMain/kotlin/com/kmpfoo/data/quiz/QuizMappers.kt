package com.kmpfoo.data.quiz

import com.kmpfoo.domain.service.quiz.Answer
import com.kmpfoo.domain.service.quiz.Question
import com.kmpfoo.domain.service.quiz.Quiz

fun QuizPojo.toDomain(): Quiz {
    return Quiz(
        theme = theme,
        questions = questions.map {
            it.toDomain()
        }
    )
}

fun QuestionPojo.toDomain(): Question {
    return Question(
        question = question,
        answers = answers.map {
            it.toDomain()
        }
    )
}

fun AnswerPojo.toDomain(): Answer {
    return Answer(
        answer = text,
        explanation = explanation,
        correct = correct,
    )
}
