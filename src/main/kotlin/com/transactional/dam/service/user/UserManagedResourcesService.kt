package com.transactional.dam.service.user

import com.transactional.dam.model.ApplicableManagedResources
import com.transactional.dam.model.User
import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import com.transactional.dam.service.exception.InvalidRequestException
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class UserManagedResourcesService(@Inject private val monolithClient: com.transactional.dam.http.monolith.MonolithClient) {

    suspend fun getManagedResources(user: User): ApplicableManagedResources {
        return when (user.role) {
            in User.Role.adminRoles -> ApplicableManagedResources(
                allSalesPartnerResources = true,
                salesPartnerId = user.salesPartnerId
            )

            else -> {
                val selection = ManagedResourcesSelection(allManagedResources = true)
                val (businessIds, locationIds, locationGroupIds) = monolithClient.getBusinessIdsLocationGroupIdsAndLocationIds(user, selection)
                ApplicableManagedResources(
                    businessIds = businessIds,
                    locationIds = locationIds,
                    locationGroupIds = locationGroupIds,
                    salesPartnerId = user.salesPartnerId
                )
            }
        }
    }

    suspend fun getManagedResources(user: User, selection: ManagedResourcesSelection): ApplicableManagedResources {
        if (user.role in User.Role.locationManagerRoles && !selection.businessIds.isNullOrEmpty()) {
            throw InvalidRequestException("User with a location manager role cannot specify businesses for assets")
        }

        if (selection.allManagedResources) {
            return getResourcesForSelectedAllManagedResources(user, selection)
        }

        if (selection.businessIds.isNullOrEmpty() && selection.locationIds.isNullOrEmpty() && selection.locationGroupIds.isNullOrEmpty()) {
            throw InvalidRequestException(
                "One of these parameters must be specified: [allManagedResources = true, businessIds, locationIds, locationGroupIds]"
            )
        }

        val excludedLocationIds = selection.excludedLocationIds ?: emptyList()
        val businessIds: MutableSet<Long> = mutableSetOf()
        val locationIds: MutableSet<Long> = mutableSetOf()
        val locationGroupIds: MutableSet<Long> = mutableSetOf()

        if (!selection.businessIds.isNullOrEmpty()) {
            businessIds.addAll(monolithClient.getBusinessIds(user, selection))
        }
        if (!selection.locationIds.isNullOrEmpty()) {
            locationIds.addAll(monolithClient.getLocationIds(user, ManagedResourcesSelection(locationIds = selection.locationIds)))
        }
        if (!selection.locationGroupIds.isNullOrEmpty()) {
            locationGroupIds.addAll(monolithClient.getLocationGroupIds(user, ManagedResourcesSelection(locationGroupIds = selection.locationGroupIds)))
        }
        return ApplicableManagedResources(businessIds = businessIds.toList(), locationIds = locationIds.toList(), excludedLocationIds = excludedLocationIds, locationGroupIds = locationGroupIds.toList())
    }

    private suspend fun getResourcesForSelectedAllManagedResources(
        user: User,
        selection: ManagedResourcesSelection
    ): ApplicableManagedResources {
        var businessIds: List<Long> = emptyList()
        var locationIds: List<Long> = emptyList()
        val excludedLocationIds = selection.excludedLocationIds ?: emptyList()

        when (user.role) {
            in User.Role.adminRoles, in User.Role.businessManagerRoles ->
                businessIds = monolithClient.getBusinessIds(user, selection)
            else -> {
                locationIds = monolithClient.getLocationIds(user, selection)
            }
        }

        return ApplicableManagedResources(businessIds = businessIds, locationIds = locationIds, excludedLocationIds = excludedLocationIds)
    }

    suspend fun getFilteredListResources(user: User, selection: ManagedResourcesSelection): ApplicableManagedResources {
        val (businessIds, locationIds, locationGroupIds) = monolithClient.getBusinessIdsLocationGroupIdsAndLocationIds(user, selection)
        return ApplicableManagedResources(
            businessIds = businessIds,
            locationIds = locationIds,
            locationGroupIds = locationGroupIds,
            salesPartnerId = user.salesPartnerId
        )
    }
}
