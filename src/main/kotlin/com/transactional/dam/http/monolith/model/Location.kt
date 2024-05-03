package com.transactional.dam.http.monolith.model

data class Location(
    val id: Long,
    val businessId: Long? = null,
    val groups: List<LocationGroup>?
)
