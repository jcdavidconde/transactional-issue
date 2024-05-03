package com.transactional.dam.handler.exception

import com.transactional.dam.extension.logSentryError
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Produces
@Singleton
@Requires(classes = [ExceptionHandler::class, Exception::class])
class GlobalExceptionHandler : ExceptionHandler<Exception, HttpResponse<Any>> {

    override fun handle(request: HttpRequest<*>?, exception: Exception?): HttpResponse<Any> {
        val sentryId: String? = LOG.logSentryError("An uncaught exception happened", exception)

        val responseMap = mapOf("errorMessage" to "An uncaught exception happened").toMutableMap()
        if (!sentryId.isNullOrEmpty()) {
            responseMap["errorCode"] = sentryId
        }

        return HttpResponse.serverError(responseMap)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
