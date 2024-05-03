package com.transactional.dam.http.monolith.model

data class LocationsResponse(
    val count: Long,
    val max: Int,
    val offset: Long,
    val locations: List<Location>?
)
