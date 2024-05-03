package com.transactional.dam.http.monolith.model

enum class BusinessStatus {
    CREATED,
    NOT_CONFIRMED,
    ACTIVE,
    DELETED;

    companion object {
        val activeStatuses: String = ACTIVE.name
    }
}
