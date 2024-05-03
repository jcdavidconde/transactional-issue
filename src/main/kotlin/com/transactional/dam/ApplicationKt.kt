package com.transactional.dam

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.Micronaut.build
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "transactional-issue",
        version = "0.0",
        description = "New service created from Micronaut + Kotlin template"
    )
)
object ApplicationKt {
    // ApplicationKt.context can be used to get properly set up beans in classes were it usually doesn't work, e.g. kotlin extensions
    lateinit var context: ApplicationContext

    @JvmStatic
    fun main(args: Array<String>) {
        context = build()
            .args(*args)
            .packages("com.transactional.dam")
            .defaultEnvironments("local")
            .start()
    }
}
