package com.transactional.dam.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Controller
class HomeController {
    @Tag(name = "Monitoring")
    @Operation(summary = "Returns OK")
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [ExampleObject(value = "OK")]
            )
        ]
    )
    @Get
    fun index(): String {
        return "OK"
    }
}
