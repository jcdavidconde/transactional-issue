package com.transactional.dam.service

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetBusiness
import com.transactional.dam.domain.AssetExcludedLocation
import com.transactional.dam.domain.AssetLocation
import com.transactional.dam.domain.AssetLocationGroup
import com.transactional.dam.model.ApplicableManagedResources

object AssetResourceUpdateUtils {

    fun updateLocations(
            asset: Asset,
            selectedManagedResources: ApplicableManagedResources
    ): Set<AssetLocation> =
        updateResources(
            selectedManagedResources.locationIds,
            asset.locations,
            { it.locationId },
            { id -> AssetLocation(asset = asset, locationId = id) }
        )

    fun updateBusinesses(
            asset: Asset,
            selectedManagedResources: ApplicableManagedResources
    ): Set<AssetBusiness> =
        updateResources(
            selectedManagedResources.businessIds,
            asset.businesses,
            { it.businessId },
            { id -> AssetBusiness(asset = asset, businessId = id) }
        )

    fun updateExcludedLocations(
            asset: Asset,
            selectedManagedResources: ApplicableManagedResources
    ): Set<AssetExcludedLocation> =
        updateResources(
            selectedManagedResources.excludedLocationIds,
            asset.excludedLocations,
            { it.excludedLocationId },
            { id -> AssetExcludedLocation(asset = asset, excludedLocationId = id) }
        )

    fun updateLocationGroups(
            asset: Asset,
            selectedManagedResources: ApplicableManagedResources
    ): Set<AssetLocationGroup> =
        updateResources(
            selectedManagedResources.locationGroupIds,
            asset.locationGroups,
            { it.locationGroupId },
            { id -> AssetLocationGroup(asset = asset, locationGroupId = id) }
        )

    private fun <T, K> updateResources(
        selectedResources: List<K>,
        assetResources: Set<T>,
        idSelector: (T) -> K,
        resourceCreator: (K) -> T
    ): MutableSet<T> {
        val updated = mutableSetOf<T>()
        val existing = assetResources.associateBy(idSelector)

        for (id in selectedResources) {
            val existingItem = existing[id]
            if (existingItem != null) {
                updated.add(existingItem)
            } else {
                val newItem = resourceCreator(id)
                updated.add(newItem)
            }
        }

        return updated
    }
}
