package com.transactional.dam.service.sentry

import com.transactional.dam.extension.toRequest
import io.micronaut.http.HttpRequest
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.slf4j.LoggerFactory

class SentryService {

    companion object {
        private val LOG = LoggerFactory.getLogger(SentryService::class.java)

        fun logSentryMessage(messageString: String, level: SentryLevel, throwable: Throwable?, httpRequest: HttpRequest<Any>?): String? {
            if (!Sentry.isEnabled()) {
                logEvent(messageString, level, throwable)
                return null
            }

            val message = Message()
            message.formatted = messageString
            message.message = messageString

            val sentryEvent = SentryEvent(throwable)
            sentryEvent.level = level
            sentryEvent.message = message
            sentryEvent.request = httpRequest?.toRequest()

            Sentry.captureEvent(sentryEvent)
            logEvent(messageString, level, throwable)

            return sentryEvent.eventId?.toString()
        }

        private fun logEvent(message: String, level: SentryLevel, throwable: Throwable?) {
            when (level) {
                SentryLevel.INFO -> LOG.info(message, throwable)
                SentryLevel.WARNING -> LOG.warn(message, throwable)
                SentryLevel.ERROR -> LOG.error(message, throwable)
                else -> LOG.debug(message, throwable)
            }
        }
    }
}
