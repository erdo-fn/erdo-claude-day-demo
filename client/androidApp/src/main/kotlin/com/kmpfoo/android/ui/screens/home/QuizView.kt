package com.kmpfoo.android.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kmpfoo.domain.DomainError
import com.kmpfoo.domain.feature.quiz.QuizState
import com.kmpfoo.domain.feature.quiz.Response
import com.kmpfoo.domain.service.quiz.Question

@Composable
fun QuizView(
    quizState: QuizState,
    selectAnswer: (Int, Int) -> Unit = { _, _ -> },
    resetResponses: () -> Unit = {},
    setBaseUrl: (String) -> Unit = { _ -> },
    setQuizTheme: (String) -> Unit = { _ -> },
    fetchQuiz: () -> Unit = { },
) {
    val focusManager = LocalFocusManager.current
    var showUrlSettings by remember { mutableStateOf(false) }
    var themeText by remember(quizState.quiz?.theme) { mutableStateOf(quizState.quiz?.theme ?: "") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            // --- Header & Settings ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quiz Master 🚀",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showUrlSettings = !showUrlSettings }) {
                    Icon(
                        if (showUrlSettings) Icons.Default.KeyboardArrowUp else Icons.Default.Settings,
                        contentDescription = "Toggle Settings"
                    )
                }
            }

            AnimatedVisibility(visible = showUrlSettings) {
                OutlinedTextField(
                    value = quizState.baseUrl,
                    onValueChange = setBaseUrl,
                    label = { Text("Base Server URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // --- Theme & Controls ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = themeText,
                        onValueChange = { themeText = it },
                        label = { Text("Quiz Theme") },
                        placeholder = { Text("e.g. 90s Pop Culture") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) setQuizTheme(themeText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = fetchQuiz,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Fetch")
                        }
                        OutlinedButton(
                            onClick = resetResponses,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            // --- Error Display ---
            if (quizState.error != DomainError.NoError) {
                Text(
                    text = "Oops! ${quizState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- Score Indicator ---
            val score = quizState.responses.count {
                it is Response.Responded &&
                        quizState.quiz?.questions?.getOrNull(quizState.responses.indexOf(it))
                            ?.answers?.getOrNull(it.answerIndex)?.correct == true
            }

            Text(
                text = "Score: $score / ${quizState.quiz?.questions?.size ?: 0}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // --- Quiz Content ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                quizState.quiz?.let { quiz ->
                    itemsIndexed(quiz.questions) { qIdx, question ->
                        QuestionCard(
                            index = qIdx + 1,
                            question = question,
                            response = quizState.responses.getOrNull(qIdx) ?: Response.NoResponse,
                            onAnswerSelected = { aIdx -> selectAnswer(qIdx, aIdx) }
                        )
                    }
                }
            }
        }

        // --- Loading Overlay ---
        if (quizState.loading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    index: Int,
    question: Question,
    response: Response,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Question $index",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            question.answers.forEachIndexed { aIdx, answer ->
                val isSelected = (response as? Response.Responded)?.answerIndex == aIdx
                val hasResponded = response is Response.Responded

                val buttonColor = when {
                    isSelected && answer.correct -> Color(0xFF4CAF50) // Green
                    isSelected && !answer.correct -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }

                Button(
                    onClick = { if (!hasResponded) onAnswerSelected(aIdx) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !hasResponded || isSelected
                ) {
                    Text(answer.answer)
                }

                if (isSelected && answer.correct && !answer.explanation.isNullOrBlank()) {
                    Text(
                        text = "💡 ${answer.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}