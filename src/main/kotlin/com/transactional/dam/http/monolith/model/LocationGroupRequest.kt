package com.transactional.dam.http.monolith.model

import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import io.micronaut.core.annotation.Introspected

@Introspected
data class LocationGroupRequest(
    var max: Int = 10000,
    var offset: Long = 0,
    var locationGroupIds: List<Long>? = null
) {
    companion object {

        fun from(selection: ManagedResourcesSelection): LocationGroupRequest {
            val request = LocationGroupRequest()
            request.locationGroupIds = selection.locationGroupIds
            return request
        }
    }
}
