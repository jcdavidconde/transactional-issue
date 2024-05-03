package com.transactional.dam.http.monolith.model

enum class LocationStatus {
    CREATED,
    ACTIVE,
    INACTIVE,
    CANCELLED,
    DELETED,
    CLOSED;

    companion object {
        val activeStatuses: List<String> = listOf(ACTIVE.name, CANCELLED.name)
    }
}
