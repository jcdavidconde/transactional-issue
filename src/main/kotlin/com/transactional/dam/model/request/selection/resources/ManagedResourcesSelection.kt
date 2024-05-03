package com.transactional.dam.model.request.selection.resources

import com.transactional.dam.model.request.CreateAssetRequest
import com.transactional.dam.model.request.ListAssetRequest
import com.transactional.dam.model.request.ListFolderRequest
import com.transactional.dam.model.request.UpdateAssetRequest
import com.transactional.dam.service.exception.InvalidRequestException

data class ManagedResourcesSelection(
    val allManagedResources: Boolean = false,
    val labels: List<String>? = null,
    val businessIds: List<Long>? = null,
    val locationIds: List<Long>? = null,
    val excludedLocationIds: List<Long>? = null,
    val locationGroupIds: List<Long>? = null
) {
    companion object {
        fun from(request: CreateAssetRequest): ManagedResourcesSelection {
            val selection = ManagedResourcesSelection(
                request.allManagedResources,
                request.labels,
                request.businessIds,
                request.locationIds,
                request.excludedLocationIds,
                request.locationGroupIds
            )
            validateResourcesSelection(selection)
            return selection
        }

        fun from(request: ListAssetRequest): ManagedResourcesSelection? {
            return if (request.locationIds.isNullOrEmpty() && request.businessIds.isNullOrEmpty()) {
                return null
            } else {
                ManagedResourcesSelection(
                    locationIds = request.locationIds,
                    businessIds = request.businessIds
                )
            }
        }

        fun from(request: ListFolderRequest): ManagedResourcesSelection? {
            return if (request.locationIds.isNullOrEmpty()) {
                null
            } else {
                ManagedResourcesSelection(locationIds = request.locationIds)
            }
        }

        fun from(request: UpdateAssetRequest): ManagedResourcesSelection {
            val selection = ManagedResourcesSelection(
                request.allManagedResources,
                request.labels,
                request.businessIds,
                request.locationIds,
                request.excludedLocationIds,
                request.locationGroupIds
            )
            validateResourcesSelection(selection)
            return selection
        }

        private fun validateResourcesSelection(selection: ManagedResourcesSelection) {
            when {
                selection.allManagedResources -> if (!selection.labels.isNullOrEmpty() || !selection.businessIds.isNullOrEmpty() || !selection.locationIds.isNullOrEmpty() || !selection.locationGroupIds.isNullOrEmpty()) {
                    throw InvalidRequestException("When allManagedResources is true, labels, businessIds, locationIds and/or locationGroupIds should not be specified")
                }

                !selection.businessIds.isNullOrEmpty() -> if (!selection.labels.isNullOrEmpty()) {
                    throw InvalidRequestException("When businessIds are provided, labels should not be specified")
                }

                !selection.locationIds.isNullOrEmpty() -> if (!selection.excludedLocationIds.isNullOrEmpty() || !selection.labels.isNullOrEmpty()) {
                    throw InvalidRequestException("When locationIds are provided, excludedLocationIds or labels should not be specified")
                }

                !selection.locationGroupIds.isNullOrEmpty() -> return

                else -> throw InvalidRequestException("One of these parameters must be specified: [allManagedResources = true, labels, businessIds, locationIds, locationGroupIds]")
            }
        }
    }
}
