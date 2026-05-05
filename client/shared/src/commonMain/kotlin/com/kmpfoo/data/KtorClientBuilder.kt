package com.kmpfoo.data

import co.early.fore.core.delegate.Fore
import co.early.fore.core.logging.Logger
import co.early.fore.net.ForeNetworkLogs
import co.early.fore.net.NetworkingLogSanitizer
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Most of this will all be specific to your application, when customising for your own case
 * bare in mind that you should also be able to use this class in your tests to mock the server
 * by passing offline data interceptor plugins in (see unit tests for an example)
 */
object KtorClientBuilder {

    /**
     *
     * @param configurePluginsBefore ktor plugin configuration block to be run first
     * NB the logging plugin is usually the last one assuming you want to log what is actually
     * sent from the device after all the other plugins have run
     * @return ktor HttpClient object suitable for instantiating service interfaces
     */
    fun create(
        lgr: Logger = Fore.getLogger(),
        json: Json,
        configurePluginsBefore: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient {

        return HttpClient { // engine resolved per platform (CIO on Android, Darwin on iOS)
            expectSuccess = true

            defaultRequest {
                header("ngrok-skip-browser-warning", "true")
            }

            configurePluginsBefore(this)

            install(ContentNegotiation) {
                json(json)
            }

            install(PluginGlobalInterceptor) {
                logger = lgr
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 120_000
            }

            install(ForeNetworkLogs) {
                // all these are optional, default will suit most requirements
                logger = lgr
                curlStyleRequestLogs = true
                prettifyResponseLogs = true
                networkingLogSanitizer = object : NetworkingLogSanitizer {
                    override fun sanitizeHeaders(headers: Set<Map.Entry<String, List<String>>>): Set<Map.Entry<String, List<String>>> {
                        return headers
                            .filterNot { header ->
                                setOf(
                                    "Authorization",
                                    "X-Auth-Token",
                                    "X-Session-Token"
                                ).any { it.equals(header.key, ignoreCase = true) }
                            }
                            .toSet()
                    }

                    override fun sanitizeBody(text: String): String {
                        // highly implementation specific, e.g. you might want
                        // to recursively search through json keys for "id" or whatever
                        // and redact all the corresponding values etc
                        return text.replace("secret", "XXXX")
                    }
                }
                filters = listOf { request ->
                    // don't log any hosts containing notinteresting.com
                    !request.url.host.contains("notinteresting.com")
                }
            }
        }
    }
}
