package com.transactional.dam.http.monolith.model

data class LocationGroup(
    val id: Long,
    val locations: List<LocationGroupLocationResponse>?
)
