package com.transactional.dam.http.monolith.model

data class MonolithResponse<T>(
    val status: String,
    val response: T?
)
