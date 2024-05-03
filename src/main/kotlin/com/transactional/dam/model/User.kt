package com.transactional.dam.model

data class User(
    val id: Long,
    val salesPartnerId: Long,
    val role: Role,
    val features: Set<Feature>,
    val accessToken: String
) {

    fun hasFeature(feature: Feature) = features.contains(feature)

    enum class Role {
        UBER_API_ADMIN,
        API_ADMIN,
        ADMIN,
        ACCOUNT_MANAGER,
        BUSINESS_MANAGER,
        BUSINESS_MANAGER_INBOX,
        LOCATION_MANAGER;

        companion object {
            val adminRoles: Set<Role> = setOf(API_ADMIN, ADMIN)
            val businessManagerRoles: Set<Role> = setOf(ACCOUNT_MANAGER, BUSINESS_MANAGER, BUSINESS_MANAGER_INBOX)
            val locationManagerRoles: Set<Role> = setOf(LOCATION_MANAGER)
        }
    }

    enum class Feature {
        DAM
    }
}
