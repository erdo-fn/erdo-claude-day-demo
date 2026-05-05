package com.kmpfoo.data

import co.early.fore.core.delegate.Fore
import co.early.fore.core.logging.Logger
import co.early.fore.net.MessageProvider
import co.early.fore.net.wrap.ErrorHandler
import com.kmpfoo.data.DataError.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.serialization.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/**
 * KMP-compatible error handler for REST API calls.
 *
 * Something like this will be suitable for most HTTP services, consider
 * customizing for the particularities of your own server side
 *
 * Retry logic is handled in KtorBuilder
 */
class ErrorHandlerRest(
    private val logWrapper: Logger? = null,
) : ErrorHandler<DataError> {


    override suspend fun <CE : MessageProvider<DataError>> handleError(
        t: Throwable,
        kSerializer: KSerializer<CE>?
    ): DataError {

        Fore.getLogger(logWrapper).e("handling error in global error handler", t)

        val errorMessage = when (t) {

            is ResponseException -> {

                //initial error type
                var msg = when (t) {
                    is ClientRequestException -> {
                        Server
                    } // in 400..499
                    is RedirectResponseException -> {
                        Server
                    } //in 300..399
                    is ServerResponseException -> {
                        Server
                    } //in 500..599
                    else -> {
                        Network
                    } //something else
                }

                val response = t.response

                Fore.getLogger(logWrapper).e("handleError() HTTP:" + response.status)

                //get more specific with the error type
                msg = when (response.status.value) {
                    401 -> SessionTimedOut
                    400, 405 -> Client
                    429 -> RateLimited
                    404 -> Server //if this happens in prod, it's usually a server config issue
                    else -> null
                } ?: msg

                // kSerializer for custom error parsing, e.g. if your server sends specific error codes in the body that you want to handle

                msg
            }

            is NoTransformationFoundException -> Server // content type is probably wrong, check response from server in app logs
            is JsonConvertException, is SerializationException -> Server //parsing issue, maybe response is not json, or does not match expected type, or is empty
            is TimeoutCancellationException -> Network // network timeout

            else -> {
                when {
                    isProbablyNetworkError(t) -> Network
                    isProbablySecurityError(t) -> SecurityUnknown
                    else -> Unknown
                }
            }
        }

        Fore.getLogger(logWrapper).w("replyWithFailure() returning:$errorMessage")
        return errorMessage
    }

    // this works a little better in KMP where we don't have unified exceptions
    // because of platform differences
    private fun isProbablyNetworkError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("network") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("unreachable") ||
                className.contains("network") ||
                className.contains("connection") ||
                className.contains("timeout") ||
                className.contains("io")
    }

    // this works a little better in KMP where we don't have unified exceptions
    // because of platform differences
    private fun isProbablySecurityError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("ssl") ||
                message.contains("tls") ||
                message.contains("certificate") ||
                message.contains("security") ||
                className.contains("ssl") ||
                className.contains("tls") ||
                className.contains("security")
    }
}
