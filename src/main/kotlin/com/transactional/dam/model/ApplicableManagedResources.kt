package com.transactional.dam.model

data class ApplicableManagedResources(
    var allSalesPartnerResources: Boolean = false,
    var salesPartnerId: Long = 0,
    var locationIds: List<Long> = emptyList(),
    var businessIds: List<Long> = emptyList(),
    var excludedLocationIds: List<Long> = emptyList(),
    var locationGroupIds: List<Long> = emptyList()
)
