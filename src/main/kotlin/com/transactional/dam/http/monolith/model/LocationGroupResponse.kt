package com.transactional.dam.http.monolith.model

data class LocationGroupResponse(
    val count: Long,
    val max: Int,
    val offset: Int,
    val locationGroups: List<LocationGroup>?
)
