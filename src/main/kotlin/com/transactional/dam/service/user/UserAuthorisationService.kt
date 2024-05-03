package com.transactional.dam.service.user

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import com.transactional.dam.model.ApplicableManagedResources
import com.transactional.dam.model.User
import com.transactional.dam.service.FolderService
import com.transactional.dam.service.exception.OperationForbiddenException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class UserAuthorisationService(@Inject private val folderService: FolderService) {

    fun needsFeature(user: User, feature: User.Feature) {
        if (!user.hasFeature(feature)) {
            throw UserForbiddenException("User ${user.id} is missing feature $feature")
        }
    }

    /**
     * Checks if the user has access to this asset
     * For ADMIN roles the check is for the salesPartnerId
     * For Business Manager roles we check if the filtered list of businesses, locations and location groups is empty
     * For Location Manager roles we check if the filtered list of businesses,locations and location groups is empty
     * Also for Location Manager roles we check that not all his user locations are part of asset excludedLocations
     */
    fun canAccessAsset(user: User, asset: Asset, managedResources: ApplicableManagedResources) {
        when (user.role) {
            in User.Role.adminRoles -> {
                if (user.salesPartnerId != asset.salesPartnerId) {
                    throw UserForbiddenException("User ${user.id} does not have access to asset with id ${asset.id}")
                }
            }

            in User.Role.businessManagerRoles -> {
                if (asset.businesses.isEmpty() && asset.locations.isEmpty() && asset.locationGroups.isEmpty()) {
                    throw UserForbiddenException("User ${user.id} does not have access to asset with id ${asset.id}")
                }
            }

            else -> {
                if (asset.businesses.isEmpty() && asset.locations.isEmpty() && asset.locationGroups.isEmpty()) {
                    throw UserForbiddenException("User ${user.id} does not have access to asset with id ${asset.id}")
                }

                val excludedLocationIds = asset.excludedLocations.map { it.excludedLocationId }.toSet()
                if (managedResources.locationIds.minus(excludedLocationIds).isEmpty()) {
                    throw UserForbiddenException("User ${user.id} does not have access to asset with id ${asset.id}")
                }
            }
        }
    }

    fun canUpdateOrDeleteAsset(user: User, asset: Asset, managedResources: ApplicableManagedResources) {
        val userManagesAllLocations =
            (asset.locations.map { it.locationId } - managedResources.locationIds.toSet()).isEmpty()
        val userManagesAllBusinesses =
            (asset.businesses.map { it.businessId } - managedResources.businessIds.toSet()).isEmpty()
        val userManagesAllLocationGroups =
            (asset.locationGroups.map { it.locationGroupId } - managedResources.locationGroupIds.toSet()).isEmpty()

        if (user.role !in User.Role.adminRoles && !userManagesAllLocations) {
            LOG.error("User does not manage all locations of asset. User ID: ${user.id}; User Role: ${user.role}; Managed locations: ${managedResources.locationIds}; Asset locations: ${asset.locations.map { it.locationId }}")
            throw UserForbiddenException("User ${user.id} with Role ${user.role} does not manage all locations of asset ${asset.id}")
        }

        if (user.role !in User.Role.adminRoles && !userManagesAllLocationGroups) {
            LOG.error("User does not manage all location groups of the asset. User ID: ${user.id}; User Role: ${user.role}; Managed groups: ${managedResources.locationGroupIds}; Asset groups: ${asset.locationGroups.map { it.locationGroupId }}")
            throw UserForbiddenException("User ${user.id} with Role ${user.role} does not manage all groups of asset ${asset.id}")
        }

        if (user.role in User.Role.businessManagerRoles && !userManagesAllBusinesses) {
            LOG.error("User does not manage all businesses of asset. User ID: ${user.id}; User Role: ${user.role}; Managed businesses: ${managedResources.businessIds}; Asset businesses: ${asset.businesses.map { it.businessId }}")
            throw UserForbiddenException("User ${user.id} with Role ${user.role} does not manage all businesses of asset ${asset.id}")
        } else if (user.role in User.Role.locationManagerRoles && asset.businesses.isNotEmpty()) {
            LOG.error("User is location manager but asset is linked to businesses. User ID: ${user.id}; User Role: ${user.role};")
            throw UserForbiddenException("User ${user.id} with Role ${user.role} cannot update asset ${asset.id} because it is associated with at least one business")
        }
    }

    suspend fun canAccessFolder(user: User, folder: Folder, managedResources: ApplicableManagedResources) {
        if (user.id == folder.authorId) return

        when (user.role) {
            in User.Role.adminRoles -> {
                if (user.salesPartnerId != folder.assets.firstOrNull()?.salesPartnerId) {
                    throw UserForbiddenException("User ${user.id} does not have access to folder with id ${folder.id}")
                }
            }

            else -> {
                val userCountOfAssets = folderService.getUserCountOfAssetsForFolder(folder.id, managedResources)
                if (userCountOfAssets < 1) {
                    throw UserForbiddenException("User ${user.id} does not have access to folder with id ${folder.id}")
                }
            }
        }
    }

    suspend fun canAccessFolder(user: User, folderId: Long, managedResources: ApplicableManagedResources) {
        val folder = folderService.getFolder(folderId)
        canAccessFolder(user, folder, managedResources)
    }

    suspend fun canDeleteFolder(user: User, folderId: Long, managedResources: ApplicableManagedResources) {
        canAccessFolder(user, folderId, managedResources)

        if (managedResources.allSalesPartnerResources) return

        val totalCount = folderService.getTotalCountOfAssetsForFolder(folderId)

        val userAccessibleCount = folderService.getUserCountOfAssetsForFolder(folderId, managedResources)

        if (userAccessibleCount < totalCount) {
            throw OperationForbiddenException(
                user,
                "delete folder",
                "Folder $folderId has at least one asset not managed by user"
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UserAuthorisationService::class.java)
    }
}
