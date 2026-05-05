package com.kmpfoo.appdi

import co.early.fore.net.wrap.CallWrapper
import co.early.fore.net.wrap.Wrapper
import co.early.persista.PerSista
import com.kmpfoo.data.DataError
import com.kmpfoo.data.ErrorHandlerRest
import com.kmpfoo.data.KtorClientBuilder
import com.kmpfoo.data.quiz.QuizApi
import io.ktor.client.*
import kotlinx.serialization.json.Json
import okio.Path
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Rest Data Sources
 */
val dataRest = module {

    /**
     * Ktor
     */
    single<HttpClient> {
        KtorClientBuilder.create(
            json = get(),
        )
    }

    single<Json> {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    single<Wrapper<DataError>> {
        CallWrapper(
            errorHandler = ErrorHandlerRest(
                logWrapper = get(),
            )
        )
    }

    /**
     * Data Sources
     */

    single {
        QuizApi(httpClient = get())
    }

    /**
     * Persistance
     */

    single {
        PerSista(
            dataPath = get(),
            logger = get(),
            json = Json {
                allowStructuredMapKeys = true
            }
        )
    }

    single {
        filesPath(application = Globals.app)
    }
}

expect fun filesPath(application: Any? = null): Path