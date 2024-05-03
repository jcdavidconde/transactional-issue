package com.transactional.dam.http.monolith.model

import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import io.micronaut.core.annotation.Introspected

@Introspected
data class BusinessesRequest(
        var max: Int = 10000,
        var offset: Long = 0,
        val fieldMask: List<String> = com.transactional.dam.http.monolith.model.BusinessesRequest.Companion.fieldMaskMandatoryFields,
        val status: List<String> = listOf(com.transactional.dam.http.monolith.model.BusinessStatus.Companion.activeStatuses),
        val features: List<String> = com.transactional.dam.http.monolith.model.ProductPlan.Companion.features,
        var businessIds: List<Long>? = null
) {
    companion object {
        val fieldMaskMandatoryFields = listOf("id", "productPlan")

        fun from(selection: ManagedResourcesSelection): com.transactional.dam.http.monolith.model.BusinessesRequest {
            val request = com.transactional.dam.http.monolith.model.BusinessesRequest()
            request.businessIds = selection.businessIds
            return request
        }
    }
}
