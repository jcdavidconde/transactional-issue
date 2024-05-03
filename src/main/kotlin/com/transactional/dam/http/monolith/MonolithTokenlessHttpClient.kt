package com.transactional.dam.http.monolith

import com.transactional.dam.http.monolith.model.LocationsRequest
import com.transactional.dam.http.monolith.model.LocationsResponse
import com.transactional.dam.http.monolith.model.MonolithResponse
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${monolith.api.tokenless.baseUrl}")
@Headers(
        Header(name = HttpHeaders.USER_AGENT, value = "DAM Service"),
        Header(name = HttpHeaders.ACCEPT, value = MediaType.APPLICATION_JSON)
)
interface MonolithTokenlessHttpClient {

    @Post("/locations/?v=\${monolith.api.version}")
    @Headers(
            Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON),
            Header(name = "X-Http-Method-Override", value = "GET")
            // For local testing, uncomment the following header that would be injected by Nginx:
            // Header(name = "X-Uberall-Client-SSL-Verify", value = "SUCCESS")
    )
    suspend fun getLocations(
            @Header("X-Uberall-Certificate-Auth-Sp-Id") salesPartnerId: Long,
            @Body requestBody: LocationsRequest
    ): HttpResponse<MonolithResponse<LocationsResponse>>
}
