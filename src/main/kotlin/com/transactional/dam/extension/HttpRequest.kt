package com.transactional.dam.extension

import com.fasterxml.jackson.databind.json.JsonMapper
import com.transactional.dam.ApplicationKt
import com.transactional.dam.config.ServerConfiguration
import io.micronaut.http.HttpRequest
import io.micronaut.kotlin.context.getBean
import io.sentry.protocol.Request
import java.util.Optional
import kotlin.collections.HashMap

/* Extension methods */

fun HttpRequest<Any>.toRequest(): Request {
    val request = Request()
    val headersMap: Map<String, String> = this.headers.asMap(String::class.java, String::class.java)

    request.headers = headersMap.maskSensitiveData().mapValues { it.value.toString() }
    request.url = getFullUrl(this.path)
    request.method = this.methodName
    request.queryString = this.uri.rawQuery

    val bodyOptional: Optional<String> = this.getBody(String::class.java)
    if (bodyOptional.isPresent) {
        val bodyMap: Map<*, *> = JsonMapper().readValue(bodyOptional.get(), HashMap::class.java)
        request.data = bodyMap.maskSensitiveData()
    }

    return request
}

/* Helper methods */

private fun getFullUrl(path: String): String {
    val serverConfiguration = ApplicationKt.context.getBean<ServerConfiguration>("serverConfiguration")
    var fullUrl: String = serverConfiguration.host ?: ""
    if (fullUrl != "" && serverConfiguration.port != null) {
        fullUrl += ":${serverConfiguration.port}"
    }
    fullUrl += path

    if (!fullUrl.startsWith("/") && !fullUrl.matches(Regex("^https?"))) {
        fullUrl = "https://$fullUrl"
    }

    return fullUrl
}
