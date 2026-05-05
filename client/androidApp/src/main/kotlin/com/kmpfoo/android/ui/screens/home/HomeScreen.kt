package com.kmpfoo.android.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import co.early.fore.compose.observeAsState
import co.early.fore.core.delegate.Fore
import co.early.fore.ui.size.*
import com.kmpfoo.android.R
import com.kmpfoo.android.ui.screens.common.toLabel
import com.kmpfoo.domain.feature.quiz.QuizModel
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    quizModel: QuizModel = koinInject(),
    size: WindowSize = LocalWindowSize.current,
) {

    val quizState by quizModel.observeAsState("FOO") { quizModel.state }

    ShowHideWrapper(quizState, size) {
        QuizView(
            quizState = quizState,
            selectAnswer = { questionIndex, answerIndex -> quizModel.selectAnswer(questionIndex, answerIndex) },
            resetResponses = { quizModel.resetResponses() },
            setBaseUrl = { baseUrl -> quizModel.updateBaseUrl(baseUrl) },
            setQuizTheme = { quizTheme -> quizModel.updateQuizTheme(quizTheme) },
            fetchQuiz = { quizModel.fetchQuiz() },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DiagnosticInfo(size)
    }
}

@Composable
fun BoxScope.DisplayToggleView(
    size: WindowSize,
    btnColor: Color,
    displayed: Boolean,
    toggleDisplayCallback: () -> Unit,
) {

    Fore.getLogger().i("DisplayToggleView")

    val minimumDimension = size.dpSize.minimumDimension()
    val toggleBtnFontSize = (minimumDimension / 30f).value.sp
    val label = stringResource(id = if (displayed) R.string.hide else R.string.show)
    val alignment = if (size.isRound) Alignment.BottomCenter else Alignment.BottomEnd

    Button(
        modifier = Modifier.align(alignment),
        colors = ButtonDefaults.textButtonColors(contentColor = btnColor),
        onClick = { toggleDisplayCallback() },
        shape = ButtonDefaults.textShape,
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = toggleBtnFontSize),
        )
    }
}

@Composable
fun BoxScope.DiagnosticInfo(size: WindowSize) {
    // Note: you don't really need two different composables here
    // as they are so similar, this code serves as the "adaptive"
    // vs "responsive" example. In this case "adaptive" meaning to
    // display a completely different UI based on the size class
    // see: https://dev.to/erdo/jetpack-compose-and-windowsize-classes-gb4
    WidthBasedComposable(
        xs = { sz -> MiniDiagnostics(sz) },
        m = { sz -> MiniDiagnostics(sz) },
        l = { sz -> RegularDiagnostics(sz) },
    )(size)
}

@Composable
fun BoxScope.RegularDiagnostics(size: WindowSize) {

    Fore.getLogger().i("RegularDiagnostics ${size.toLabel()}")

    val diagnosticsFontSize = (size.dpSize.width / 60f).value.sp
    val alignment = if (size.isRound) Alignment.BottomCenter else Alignment.BottomStart
    val diagnosticText = size.toLabel(extended = true, multipleLines = size.isRound)

    Text(
        modifier = Modifier
            .align(alignment)
            .padding(start = 10.dp, end = 10.dp)
            .background(color = Color.Yellow),
        text = diagnosticText,
        style = TextStyle(color = Color.Red, fontSize = diagnosticsFontSize)
    )
}

@Composable
fun BoxScope.MiniDiagnostics(size: WindowSize) {

    Fore.getLogger().i("MiniDiagnostics ${size.toLabel()}")

    val diagnosticsFontSize = (size.dpSize.width / 30f).value.sp
    val alignment = if (size.isRound) Alignment.BottomCenter else Alignment.BottomStart
    val diagnosticText = size.toLabel(multipleLines = size.isRound)

    Text(
        modifier = Modifier
            .align(alignment)
            .padding(start = 10.dp, end = 10.dp)
            .background(color = Color.Yellow),
        text = diagnosticText,
        style = TextStyle(color = Color.Blue, fontSize = diagnosticsFontSize)
    )
}

/**
 * This is mainly to demonstrate that [ObservableGroup.observeAsState()] works as intended,
 * as the app is backgrounded or the composable is hidden, the logs show the fore
 * observer being added / removed as appropriate. See the observeAsState code comments for a
 * full explanation
 */
@Composable
fun ShowHideWrapper(state: Any, size: WindowSize, content: @Composable () -> Unit) {

    val show = remember { mutableStateOf(true) }
    val btnColor by animateColorAsState(
        targetValue = if (show.value) Color.Red else Color.Green,
        animationSpec = tween(durationMillis = 500),
    )

    AnimatedVisibility(
        visible = show.value,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    ) {
        content()
    }

    val stateFontSize = WidthBasedTextUnit(
        xs = 12.sp,
        m = 20.sp,
        l = 35.sp
    )

    val stateAsString = state.prettyPrint()

    AnimatedVisibility(
        visible = !show.value,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stateAsString,
                style = TextStyle(fontSize = stateFontSize(size))
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DisplayToggleView(
            size = size,
            displayed = show.value,
            btnColor = btnColor,
            toggleDisplayCallback = { show.value = !show.value },
        )
    }
}

// https://gist.github.com/Mayankmkh/92084bdf2b59288d3e74c3735cccbf9f
fun Any.prettyPrint(): String {

    var indentLevel = 0
    val indentWidth = 4

    fun padding() = "".padStart(indentLevel * indentWidth)

    val toString = toString()//.replace("foo.bar.clean.domain.features.", "")

    val stringBuilder = StringBuilder(toString.length)

    var i = 0
    while (i < toString.length) {
        when (val char = toString[i]) {
            '(', '[', '{' -> {
                indentLevel++
                stringBuilder.appendLine(char).append(padding())
            }

            ')', ']', '}' -> {
                indentLevel--
                stringBuilder.appendLine().append(padding()).append(char)
            }

            ',' -> {
                stringBuilder.appendLine(char).append(padding())
                // ignore space after comma as we have added a newline
                val nextChar = toString.getOrElse(i + 1) { char }
                if (nextChar == ' ') i++
            }

            else -> {
                stringBuilder.append(char)
            }
        }
        i++
    }

    return stringBuilder.toString().replace("=", " = ")
}