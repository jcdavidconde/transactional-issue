package com.transactional.dam.http.monolith

import com.transactional.dam.http.monolith.model.BusinessesRequest
import com.transactional.dam.http.monolith.model.BusinessesResponse
import com.transactional.dam.http.monolith.model.EnrichAssetDataRequest
import com.transactional.dam.http.monolith.model.EnrichAssetDataResponse
import com.transactional.dam.http.monolith.model.LocationGroupRequest
import com.transactional.dam.http.monolith.model.LocationGroupResponse
import com.transactional.dam.http.monolith.model.LocationsRequest
import com.transactional.dam.http.monolith.model.LocationsResponse
import com.transactional.dam.http.monolith.model.MonolithResponse
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${monolith.api.baseUrl}")
@Headers(
        Header(name = HttpHeaders.USER_AGENT, value = "DAM Service"),
        Header(name = HttpHeaders.ACCEPT, value = MediaType.APPLICATION_JSON)
)
interface MonolithHttpClient {

    @Post("/locations/?v=\${monolith.api.version}&access_token={access_token}")
    @Headers(
            Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON),
            Header(name = "X-Http-Method-Override", value = "GET")
    )
    suspend fun getLocations(
            @PathVariable("access_token") accessToken: String,
            @Body requestBody: LocationsRequest
    ): HttpResponse<MonolithResponse<LocationsResponse>>

    @Post("/businesses/?v=\${monolith.api.version}&access_token={access_token}")
    @Headers(
            Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON),
            Header(name = "X-Http-Method-Override", value = "GET")
    )
    suspend fun getBusinesses(
            @PathVariable("access_token") accessToken: String,
            @Body requestBody: BusinessesRequest
    ): HttpResponse<MonolithResponse<BusinessesResponse>>

    @Post("/location-groups/?v=\${monolith.api.version}&access_token={access_token}")
    @Headers(
            Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON),
            Header(name = "X-Http-Method-Override", value = "GET")
    )
    suspend fun getLocationGroups(
            @PathVariable("access_token") accessToken: String,
            @Body requestBody: LocationGroupRequest
    ): HttpResponse<MonolithResponse<LocationGroupResponse>>

    @Post("/social-templates/enrich-data?v=\${monolith.api.version}&access_token={access_token}")
    @Headers(
            Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON)
    )
    suspend fun enrichAssetData(
            @PathVariable("access_token") accessToken: String,
            @Body requestBody: EnrichAssetDataRequest
    ): HttpResponse<MonolithResponse<EnrichAssetDataResponse>>

    @Delete("/social-posts/templates/{id}?v=\${monolith.api.version}&access_token={access_token}")
    suspend fun deleteTemplate(
            @PathVariable("access_token") accessToken: String,
            @PathVariable id: Long
    ): HttpResponse<MonolithResponse<Nothing>>
}
