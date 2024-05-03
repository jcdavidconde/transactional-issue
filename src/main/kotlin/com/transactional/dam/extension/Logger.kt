package com.transactional.dam.extension

import com.transactional.dam.service.sentry.SentryService
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.sentry.SentryLevel
import org.slf4j.Logger

fun Logger?.logSentryError(message: String, throwable: Throwable? = null): String? {
    val httpRequest: HttpRequest<Any>? = ServerRequestContext.currentRequest<Any>()?.get()
    return SentryService.logSentryMessage(message, SentryLevel.ERROR, throwable, httpRequest)
}
