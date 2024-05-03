package com.transactional.dam.http.monolith

import com.transactional.dam.http.monolith.model.Business
import com.transactional.dam.http.monolith.model.BusinessesRequest
import com.transactional.dam.http.monolith.model.BusinessesResponse
import com.transactional.dam.http.monolith.model.EnrichAssetDataRequest
import com.transactional.dam.http.monolith.model.EnrichAssetDataResponse
import com.transactional.dam.http.monolith.model.Location
import com.transactional.dam.http.monolith.model.LocationGroup
import com.transactional.dam.http.monolith.model.LocationGroupRequest
import com.transactional.dam.http.monolith.model.LocationGroupResponse
import com.transactional.dam.http.monolith.model.LocationsRequest
import com.transactional.dam.http.monolith.model.LocationsResponse
import com.transactional.dam.http.monolith.model.MonolithResponse
import com.transactional.dam.model.User
import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import com.transactional.dam.service.user.UserForbiddenException
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class MonolithClient(
        @Inject
        private val httpClient: MonolithHttpClient,

        @Inject
        private val tokenlessHttpClient: MonolithTokenlessHttpClient,

        @Value("\${monolith.api.max}")
        private val max: Int
) {

    suspend fun getBusinessIds(user: User, selection: ManagedResourcesSelection): List<Long> {
        val businesses = mutableListOf<Business>()

        val requestBody = BusinessesRequest.from(selection)
        requestBody.max = max

        do {
            val httpResponse: HttpResponse<MonolithResponse<BusinessesResponse>>
            try {
                httpResponse = httpClient.getBusinesses(user.accessToken, requestBody)
            } catch (e: Exception) {
                throw MonolithClientException("User: ${user.id}", "get managed businesses", e.message)
            }

            val businessesResponse = getResponseObject(httpResponse, "User: ${user.id}", "get managed businesses")
            businessesResponse.businesses?.let {
                businesses.addAll(it)
                requestBody.offset += it.size
            }
        } while (businesses.size < businessesResponse.count)
        return getBusinessIdsFromBusinesses(businesses)
    }

    private fun getBusinessIdsFromBusinesses(businesses: List<Business>): List<Long> {
        return businesses.map { it.id }.toSet().toList()
    }
    suspend fun getLocationGroupIds(user: User, selection: ManagedResourcesSelection): List<Long> {
        val locationGroups = mutableListOf<LocationGroup>()

        val requestBody = LocationGroupRequest.from(selection)
        requestBody.max = max

        do {
            val httpResponse: HttpResponse<MonolithResponse<LocationGroupResponse>>
            try {
                httpResponse = httpClient.getLocationGroups(user.accessToken, requestBody)
            } catch (e: Exception) {
                throw MonolithClientException("User: ${user.id}", "get managed location group ids", e.message)
            }

            val locationGroupResponse = getResponseObject(httpResponse, "User: ${user.id}", "get managed location groups")
            locationGroupResponse.locationGroups?.let {
                locationGroups.addAll(it)
                requestBody.offset += it.size
            }
        } while (locationGroups.size < locationGroupResponse.count)

        val locationFromLocationGroupIds = locationGroups.flatMap { it.locations?.toList() ?: emptyList() }.flatMap { listOf(it.id) }
        val locationsFromGroupsWithDAMFeature = getLocationIds(user, ManagedResourcesSelection(locationIds = locationFromLocationGroupIds))

        return filterLocationGroupsWithLocationsWithDAMFeature(locationGroups, locationsFromGroupsWithDAMFeature)
    }

    /**
     * Returns the ids of the location groups in which at least one of the locations have DAM Product Plan feature
     */
    private fun filterLocationGroupsWithLocationsWithDAMFeature(locationGroups: List<LocationGroup>, managedLocationsWithDAMFeature: List<Long>): List<Long> {
        return locationGroups.filter { locationGroup ->
            locationGroup.locations?.flatMap { listOf(it.id) }?.intersect(managedLocationsWithDAMFeature.toSet())?.isNotEmpty() ?: false
        }.map { it.id }.toList()
    }

    /**
     * Returns the ids for the user's managed locations which have the DAM Product Plan Feature enabled
     */
    suspend fun getLocationIds(user: User, selection: ManagedResourcesSelection): List<Long> {
        return getLocationIds("User ${user.id}", selection) { body: LocationsRequest ->
            httpClient.getLocations(user.accessToken, body)
        }
    }

    /**
     * Returns the ids for the sales partner's locations which have the DAM Product Plan Feature enabled
     */
    suspend fun getLocationIds(salesPartnerId: Long, selection: ManagedResourcesSelection): List<Long> {
        return getLocationIds("SP $salesPartnerId", selection) { body: LocationsRequest ->
            tokenlessHttpClient.getLocations(salesPartnerId, body)
        }
    }

    suspend fun deleteTemplate(user: User, id: Long) {
        try {
            httpClient.deleteTemplate(user.accessToken, id)
        } catch (e: Exception) {
            throw if (e is HttpClientResponseException && e.response.status == HttpStatus.FORBIDDEN) {
                UserForbiddenException("User ${user.id} is forbidden from deleting template with ID $id")
            } else {
                MonolithClientException("User ${user.id}", "delete template", e.message)
            }
        }
    }

    private suspend fun getLocationIds(
            callInfo: String,
            selection: ManagedResourcesSelection,
            httpCall: suspend (requestBody: LocationsRequest) -> HttpResponse<MonolithResponse<LocationsResponse>>
    ): List<Long> {
        val requestBody = LocationsRequest.from(selection)
        requestBody.max = max

        val locations = mutableListOf<Location>()
        do {
            val httpResponse: HttpResponse<MonolithResponse<LocationsResponse>>
            try {
                httpResponse = httpCall(requestBody)
            } catch (e: Exception) {
                throw MonolithClientException(callInfo, "get managed locations", e.message)
            }

            val locationsResponse = getResponseObject(httpResponse, callInfo)
            locationsResponse.locations?.let {
                locations.addAll(it)
                requestBody.offset += it.size
            }
        } while (locations.size < locationsResponse.count)

        return getLocationIdsFromLocations(locations)
    }

    suspend fun getBusinessIdsLocationGroupIdsAndLocationIds(
            user: User,
            selection: ManagedResourcesSelection
    ): Triple<List<Long>, List<Long>, List<Long>> {
        val requestBody = LocationsRequest.from(selection)
        requestBody.max = max
        requestBody.fieldMask = LocationsRequest.fieldMaskIncludeBusinessIdAndGroups

        val locations = getLocations(user, requestBody)
        return getBusinessIdsLocationIdsAndGroupIds(locations)
    }

    private suspend fun getLocations(user: User, requestBody: LocationsRequest): List<Location> {
        val locations = mutableListOf<Location>()
        do {
            val httpResponse: HttpResponse<MonolithResponse<LocationsResponse>>
            try {
                httpResponse = httpClient.getLocations(user.accessToken, requestBody)
            } catch (e: Exception) {
                throw MonolithClientException("User: ${user.id}", "get managed locations", e.message)
            }

            val locationsResponse = getResponseObject(httpResponse, "User: ${user.id}", "get managed locations")
            locationsResponse.locations?.let {
                locations.addAll(it)
                requestBody.offset += it.size
            }
        } while (locations.size < locationsResponse.count)

        return locations
    }

    suspend fun enrichAssetData(user: User, request: EnrichAssetDataRequest): EnrichAssetDataResponse {
        val httpResponse: HttpResponse<MonolithResponse<EnrichAssetDataResponse>>
        try {
            httpResponse = httpClient.enrichAssetData(user.accessToken, request)
        } catch (e: Exception) {
            throw MonolithClientException("User: ${user.id}", "enrich asset data", e.message)
        }

        return getResponseObject(httpResponse, "User: ${user.id}", "enrich asset data")
    }

    private fun <T> getResponseObject(
            httpResponse: HttpResponse<MonolithResponse<T>>,
            callInfo: String,
            task: String = "get managed locations"
    ): T {
        if (httpResponse.status != HttpStatus.OK) {
            throw MonolithClientException(
                    callInfo,
                    task,
                    "http response status: ${httpResponse.status}, reason: ${httpResponse.reason()}"
            )
        }
        if (httpResponse.body.isEmpty) {
            throw MonolithClientException(
                    callInfo,
                    task,
                    "http response did not contain an uberall monolith response"
            )
        }

        val uberallResponseStatus = httpResponse.body.map { it.status }.orElse(null)
        val uberallResponseBody = httpResponse.body.map { it.response }.orElse(null)
        if (uberallResponseStatus != "SUCCESS") {
            throw MonolithClientException(
                    callInfo,
                    task,
                    "uberall monolith response status: $uberallResponseStatus"
            )
        }
        if (uberallResponseBody == null) {
            throw MonolithClientException(
                    callInfo,
                    task,
                    "uberall monolith response body is null"
            )
        }
        return uberallResponseBody
    }

    private fun getLocationIdsFromLocations(locations: List<Location>): List<Long> {
        return locations.map { it.id }.toSet().toList()
    }

    private fun getBusinessIdsLocationIdsAndGroupIds(locations: List<Location>): Triple<List<Long>, List<Long>, List<Long>> {
        val locationIds = mutableSetOf<Long>()
        val businessIds = mutableSetOf<Long>()
        val locationGroupIds = mutableSetOf<Long>()

        locations.forEach { location ->
            locationIds.add(location.id)
            location.businessId?.let { businessId -> businessIds.add(businessId) }
            location.groups?.let { locationGroups -> locationGroupIds.addAll(locationGroups.flatMap { setOf(it.id) }) }
        }
        return Triple(businessIds.toList(), locationIds.toList(), locationGroupIds.toList())
    }
}
