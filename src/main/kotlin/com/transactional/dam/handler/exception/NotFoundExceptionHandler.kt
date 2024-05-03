package com.transactional.dam.handler.exception

import com.transactional.dam.service.exception.NotFoundException
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
@Requires(classes = [ExceptionHandler::class, NotFoundException::class])
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException, HttpResponse<Any>> {

    override fun handle(request: HttpRequest<*>?, exception: NotFoundException?): HttpResponse<Any> {
        val error = JsonError("Not Found")
            .link(Link.SELF, Link.of(request?.uri))
            .embedded("errors", JsonError(exception?.message))

        return HttpResponse.status<JsonError>(HttpStatus.NOT_FOUND).body(error)
    }
}
