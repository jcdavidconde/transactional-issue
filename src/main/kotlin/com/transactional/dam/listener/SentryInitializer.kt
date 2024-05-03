package com.transactional.dam.listener

import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.sentry.Sentry
import org.slf4j.LoggerFactory

internal class SentryInitializer : ApplicationEventListener<ServerStartupEvent> {

    @Value("\${sentry.dsn:false}")
    private lateinit var sentryDsn: String

    @Value("\${app.env:local}")
    private lateinit var env: String

    @Value("\${app.version:1.0}")
    private lateinit var version: String

    override fun onApplicationEvent(event: ServerStartupEvent) {
        if (sentryDsn == "false" || env in listOf("local", "test")) {
            return
        }

        LOG.info("Sentry will be enabled with environment '$env', DSN '$sentryDsn' and version '$version'")
        Sentry.init { options ->
            options.dsn = sentryDsn
            options.environment = env
            options.release = version
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SentryInitializer::class.java)
    }
}
