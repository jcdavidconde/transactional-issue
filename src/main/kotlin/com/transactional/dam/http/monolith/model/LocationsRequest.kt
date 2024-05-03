package com.transactional.dam.http.monolith.model

import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import io.micronaut.core.annotation.Introspected

@Introspected
data class LocationsRequest(
    var max: Int = 10000,
    var offset: Long = 0,
    var fieldMask: List<String> = fieldMaskMandatoryFields,
    val status: List<String> = LocationStatus.activeStatuses,
    val features: List<String> = ProductPlan.features,
    var labels: List<String>? = null,
    var businessIds: List<Long>? = null,
    var locationIds: List<Long>? = null,
    var excludedLocationIds: List<Long>? = null,
    var locationGroupIds: List<Long>? = null,
    var selectAll: Boolean = true
) {
    companion object {
        private const val idFieldMask = "id"
        private const val featuresFieldMask = "features"
        private const val businessIdFieldMask = "businessId"
        private const val groupsFieldMask = "groups"

        val fieldMaskMandatoryFields = listOf(idFieldMask, featuresFieldMask)
        val fieldMaskIncludeBusinessIdAndGroups = listOf(idFieldMask, featuresFieldMask, businessIdFieldMask, groupsFieldMask)

        fun from(selection: ManagedResourcesSelection): LocationsRequest {
            val request = LocationsRequest()
            request.labels = selection.labels
            request.businessIds = selection.businessIds
            request.locationIds = selection.locationIds
            request.excludedLocationIds = selection.excludedLocationIds
            request.locationGroupIds = selection.locationGroupIds
            return request
        }
    }
}
