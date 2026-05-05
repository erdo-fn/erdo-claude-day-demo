package com.kmpfoo.data

import co.early.fore.core.logging.Logger
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*
import io.ktor.client.HttpClient

/**
 * Before creating your own custom ktor plugin check what's available first,
 * for instance: install(DefaultRequest) { headers.append("User-Agent", "my ua") }
 */
class PluginGlobalInterceptor private constructor(
    private val logger: Logger,
) {

    class Config {
        lateinit var logger: Logger
    }

    companion object Plugin : HttpClientPlugin<Config, PluginGlobalInterceptor> {

        override val key = AttributeKey<PluginGlobalInterceptor>("PluginGlobalInterceptor")

        override fun prepare(block: Config.() -> Unit): PluginGlobalInterceptor {
            val config = Config().apply(block)
            return PluginGlobalInterceptor(
                logger = config.logger
            )
        }

        override fun install(plugin: PluginGlobalInterceptor, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.headers.append("Content-Type", "application/json")
                // context.headers.append("X-MyApp-Auth-Token", plugin.session?.getSessionToken() ?: "expired")
                context.headers.append("ngrok-skip-browser-warning", "true") // only needed if using ngrok, cloudflare doesn't use it
                context.headers.append("User-Agent", "claude-day-example")
            }
        }
    }
}
