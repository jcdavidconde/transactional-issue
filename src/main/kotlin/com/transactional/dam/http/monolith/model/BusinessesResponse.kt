package com.transactional.dam.http.monolith.model

data class BusinessesResponse(
    val count: Long,
    val max: Int,
    val offset: Int,
    val businesses: List<Business>?
)
