package com.transactional.dam.service

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetBusiness
import com.transactional.dam.domain.AssetExcludedLocation
import com.transactional.dam.domain.AssetLocation
import com.transactional.dam.domain.AssetLocationGroup
import com.transactional.dam.domain.Folder
import com.transactional.dam.model.ApplicableManagedResources
import com.transactional.dam.model.AssetsCounts
import com.transactional.dam.model.User
import com.transactional.dam.model.request.CreateAssetRequest
import com.transactional.dam.model.request.ListAssetRequest
import com.transactional.dam.model.request.RequestUtils
import com.transactional.dam.model.request.UpdateAssetRequest
import com.transactional.dam.model.response.EnrichedAssetResponse
import com.transactional.dam.model.response.ListEnrichedAssetResponse
import com.transactional.dam.model.response.ResponseUtils.toEnrichedResponse
import com.transactional.dam.repository.AssetBusinessRepository
import com.transactional.dam.repository.AssetExcludedLocationRepository
import com.transactional.dam.repository.AssetLocationGroupRepository
import com.transactional.dam.repository.AssetLocationRepository
import com.transactional.dam.repository.AssetRepository
import com.transactional.dam.repository.FolderRepository
import com.transactional.dam.service.AssetResourceUpdateUtils.updateBusinesses
import com.transactional.dam.service.AssetResourceUpdateUtils.updateExcludedLocations
import com.transactional.dam.service.AssetResourceUpdateUtils.updateLocationGroups
import com.transactional.dam.service.AssetResourceUpdateUtils.updateLocations
import com.transactional.dam.service.exception.InvalidRequestException
import com.transactional.dam.service.exception.NotFoundException
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.transaction.annotation.ReadOnly
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime

@Singleton
open class AssetService(
        @Inject private val assetBusinessRepository: AssetBusinessRepository,
        @Inject private val assetExcludedLocationRepository: AssetExcludedLocationRepository,
        @Inject private val assetLocationGroupRepository: AssetLocationGroupRepository,
        @Inject private val assetLocationRepository: AssetLocationRepository,
        @Inject private val assetRepository: AssetRepository,
        @Inject private val folderRepository: FolderRepository,
        @Inject private val socialTemplateService: SocialTemplateService
) {

    fun retrieveAssetTypes(): Array<Asset.Type> {
        // For now, only social post templates are supported
        return arrayOf(Asset.Type.SOCIAL_POST_TEMPLATE)
    }

    @ReadOnly
    open suspend fun getAllSalesPartnerIds(): Set<Long> {
        return assetRepository.findDistinctSalesPartnerIdByStatusIn(Asset.Status.nonRemoved)
    }

    @Transactional
    open suspend fun createAsset(
        user: User,
        request: CreateAssetRequest,
        selectedManagedResources: ApplicableManagedResources
    ): Asset {
        if (selectedManagedResources.locationIds.isEmpty() && selectedManagedResources.businessIds.isEmpty() && selectedManagedResources.locationGroupIds.isEmpty()) {
            throw InvalidRequestException("Either locationIds, businessIds or locationGroupIds must be provided")
        }

        val folder: Folder = folderRepository.findByIdAndStatusNotEqual(request.folderId, Folder.Status.REMOVED)
            ?: throw NotFoundException("Folder", request.folderId)

        if (request.type.name != folder.type.name) {
            throw InvalidRequestException("Folder with folderId ${request.folderId} has a different type: ${folder.type}")
        }

        val asset = assetRepository.save(Asset.create(user, request, folder))

        asset.locations = createAssetLocations(asset, selectedManagedResources.locationIds)
        asset.businesses = createAssetBusinesses(asset, selectedManagedResources.businessIds)
        asset.excludedLocations = createAssetExcludedLocations(asset, selectedManagedResources.excludedLocationIds)
        asset.locationGroups = createAssetLocationGroups(asset, selectedManagedResources.locationGroupIds)

        return asset
    }

    /**
     * Returns an asset with the locations, businesses, excluded locations and location groups filtered based on the managed resources
     */
    @ReadOnly
    open suspend fun getAsset(id: Long, managedResources: ApplicableManagedResources): Asset {
        val asset = getAsset(id)

        with(getExistingAssetResources(asset, managedResources)) {
            asset.businesses = businesses
            asset.locations = locations
            asset.excludedLocations = excludedLocations
            asset.locationGroups = locationGroups
        }

        return asset
    }

    @Transactional
    open suspend fun incrementUsageCount(id: Long): Asset {
        val asset = getAsset(id)
        ++asset.usageCount

        return asset
    }

    @Transactional
    open suspend fun getAsset(id: Long): Asset =
        assetRepository.findByIdAndStatusNotEqual(id, Asset.Status.REMOVED)
            ?: throw NotFoundException("Asset", id)

    @Transactional
    open suspend fun findExistingTemplateIds(ids: List<Long>): List<Long> =
        assetRepository.findIdsByTemplateIdInList(ids)

    suspend fun getEnrichedAsset(user: User, asset: Asset): EnrichedAssetResponse {
        val templateInfo = socialTemplateService.enrichAssetData(listOf(asset), user).first()
        return asset.toEnrichedResponse(templateInfo)
    }

    suspend fun listEnrichedAssets(
        user: User,
        request: ListAssetRequest,
        managedResources: ApplicableManagedResources
    ): ListEnrichedAssetResponse {
        val assets = listAssets(request, managedResources)
        val templates = socialTemplateService.enrichAssetData(assets.content, user)
        return assets.toEnrichedResponse(templates)
    }

    @ReadOnly
    open suspend fun listAssets(
        request: ListAssetRequest,
        managedResources: ApplicableManagedResources
    ): Page<Asset> {
        val pageable = Pageable.from(request.page, request.size)

        val assetPage = if (request.type != null) {
            listAssetsByType(request, managedResources, pageable)
        } else {
            listAssetsByFolderId(request, managedResources, pageable)
        }

        populateAssetResources(assetPage, managedResources)
        return assetPage
    }

    private suspend fun listAssetsByType(
        request: ListAssetRequest,
        managedResources: ApplicableManagedResources,
        pageable: Pageable
    ): Page<Asset> {
        val assetStatuses = if (request.status != null) listOf(request.status) else Asset.Status.nonRemoved
        val folderStatuses = request.folderStatuses ?: Folder.Status.nonRemoved
        val queryString = RequestUtils.formatSearchQuery(request.query)

        return listAssetsByTypeAndStatus(
            request.type!!,
            queryString,
            request.folderIds,
            assetStatuses,
            folderStatuses,
            managedResources,
            pageable
        )
    }

    private suspend fun listAssetsByTypeAndStatus(
            type: Asset.Type,
            queryString: String?,
            folderIds: List<Long>?,
            statuses: List<Asset.Status>,
            folderStatuses: List<Folder.Status>,
            managedResources: ApplicableManagedResources,
            pageable: Pageable
    ): Page<Asset> {
        if (managedResources.allSalesPartnerResources) {
            return assetRepository.findAssetsByTypeAndSalesPartner(
                type.toString(),
                queryString,
                folderIds,
                statuses.map { it.toString() },
                folderStatuses.map { it.toString() },
                managedResources.salesPartnerId,
                pageable
            )
        }

        return assetRepository.findAssetsByTypeAndResources(
            type.toString(),
            queryString,
            folderIds,
            statuses.map { it.toString() },
            folderStatuses.map { it.toString() },
            managedResources.locationIds,
            managedResources.businessIds,
            managedResources.locationIds.size,
            managedResources.locationGroupIds,
            managedResources.salesPartnerId,
            pageable
        )
    }

    private suspend fun listAssetsByFolderId(
        request: ListAssetRequest,
        managedResources: ApplicableManagedResources,
        pageable: Pageable
    ): Page<Asset> {
        val queryString = RequestUtils.formatSearchQuery(request.query)

        if (request.status != null) {
            return listAssetByFolderIdAndStatus(request.folderIds?.first()!!, listOf(request.status), managedResources, queryString, pageable)
        }
        return listAssetByFolderIdAndStatus(request.folderIds?.first()!!, Asset.Status.nonRemoved, managedResources, queryString, pageable)
    }

    private suspend fun listAssetByFolderIdAndStatus(
            folderId: Long,
            statuses: List<Asset.Status>,
            managedResources: ApplicableManagedResources,
            query: String?,
            pageable: Pageable
    ): Page<Asset> {
        if (managedResources.allSalesPartnerResources) {
            return assetRepository.findAssetsByFolderIdAndSalesPartner(
                folderId,
                statuses.map { it.toString() },
                managedResources.salesPartnerId,
                query,
                pageable
            )
        }

        return assetRepository.findAssetsByFolderIdAndResources(
            folderId,
            statuses.map { it.toString() },
            managedResources.locationIds,
            managedResources.businessIds,
            managedResources.locationIds.size,
            managedResources.locationGroupIds,
            query,
            managedResources.salesPartnerId,
            pageable
        )
    }

    @ReadOnly
    open suspend fun listAssets(
        folderId: Long,
        locationIds: List<Long>?,
        businessIds: List<Long>?,
        locationGroupIds: List<Long>?,
        query: String?,
        salesPartnerId: Long,
        pageable: Pageable
    ): Page<Asset> {
        return assetRepository.findAssetsByFolderIdAndResources(
            folderId = folderId,
            statuses = Asset.Status.nonRemoved.map { it.toString() },
            locations = locationIds,
            businesses = businessIds,
            locationsSize = locationIds?.size ?: 0,
            locationGroups = locationGroupIds,
            query = RequestUtils.formatSearchQuery(query),
            salesPartnerId = salesPartnerId,
            pageable = pageable
        )
    }

    @ReadOnly
    open suspend fun listAssets(salesPartnerId: Long, pageable: Pageable): Page<Asset> {
        val assetPage = assetRepository.findBySalesPartnerIdAndStatusIn(
            salesPartnerId,
            Asset.Status.nonRemoved,
            pageable
        )
        populateAssetLocations(
            assetPage,
            ApplicableManagedResources(allSalesPartnerResources = true, salesPartnerId = salesPartnerId)
        )
        return assetPage
    }

    @Transactional
    open suspend fun updateAsset(
        id: Long,
        updateAssetRequest: UpdateAssetRequest,
        selectedManagedResources: ApplicableManagedResources,
        managedResources: ApplicableManagedResources
    ): Asset {
        val asset = getAsset(id)

        if (updateAssetRequest.folderId != null && updateAssetRequest.folderId != asset.folder?.id) {
            val folder = folderRepository.findByIdAndStatusNotEqual(updateAssetRequest.folderId, Folder.Status.REMOVED)
                ?: throw NotFoundException("Folder", updateAssetRequest.folderId)
            asset.folder = folder
        }

        asset.name = updateAssetRequest.name ?: asset.name
        asset.description = updateAssetRequest.description
        asset.status = updateAssetRequest.status ?: asset.status
        asset.startDate = updateAssetRequest.startDate ?: asset.startDate
        asset.endDate = updateAssetRequest.endDate

        with(getResourcesToUpdate(asset, selectedManagedResources, managedResources)) {
            asset.locations = locations
            asset.businesses = businesses
            asset.excludedLocations = excludedLocations
            asset.locationGroups = locationGroups
        }

        return asset
    }

    @Transactional
    open suspend fun deleteAsset(user: User, id: Long) {
        val asset = getAsset(id)

        socialTemplateService.deleteTemplate(asset, user)

        asset.status = Asset.Status.REMOVED
        assetRepository.update(asset)
    }

    @Transactional
    open suspend fun activateAssets(assetStartDate: LocalDate): Long {
        return assetRepository.updateStatusAndDateUpdatedByStartDateAndStatus(
            Asset.Status.VISIBLE,
            LocalDateTime.now(),
            assetStartDate,
            Asset.Status.HIDDEN
        )
    }

    @Transactional
    open suspend fun deactivateAssets(assetEndDate: LocalDate): Long {
        return assetRepository.updateStatusAndDateUpdatedByEndDateAndStatus(
            Asset.Status.HIDDEN,
            LocalDateTime.now(),
            assetEndDate,
            Asset.Status.VISIBLE
        )
    }

    @ReadOnly
    open suspend fun countAssets(folderId: Long, managedResources: ApplicableManagedResources): AssetsCounts {
        return if (managedResources.allSalesPartnerResources) {
            assetRepository.countVisibleAndTotalByFolderIdAndSalesPartnerId(folderId, managedResources.salesPartnerId)
        } else {
            assetRepository.countVisibleAndTotalAssets(
                folderId,
                managedResources.locationIds,
                managedResources.businessIds,
                managedResources.locationIds.size,
                managedResources.locationGroupIds,
                managedResources.salesPartnerId
            )
        }
    }

    private suspend fun createAssetLocations(asset: Asset, locationIds: List<Long>): Set<AssetLocation> {
        return locationIds.map { createAssetLocation(asset, it) }.toSet()
    }

    private suspend fun createAssetBusinesses(asset: Asset, businessIds: List<Long>): Set<AssetBusiness> {
        return businessIds.map { createAssetBusiness(asset, it) }.toSet()
    }

    private suspend fun createAssetExcludedLocations(
            asset: Asset,
            excludedLocationIds: List<Long>
    ): Set<AssetExcludedLocation> {
        return excludedLocationIds.map { createAssetExcludedLocation(asset, it) }.toSet()
    }

    private suspend fun createAssetLocationGroups(
            asset: Asset,
            locationGroupIds: List<Long>
    ): Set<AssetLocationGroup> {
        return locationGroupIds.map { createAssetLocationGroup(asset, it) }.toSet()
    }

    private suspend fun createAssetLocation(asset: Asset, locationId: Long): AssetLocation {
        return assetLocationRepository.save(AssetLocation(asset, locationId))
    }

    private suspend fun createAssetBusiness(asset: Asset, businessId: Long): AssetBusiness {
        return assetBusinessRepository.save(AssetBusiness(asset, businessId))
    }

    private suspend fun createAssetExcludedLocation(asset: Asset, excludedLocationId: Long): AssetExcludedLocation {
        return assetExcludedLocationRepository.save(AssetExcludedLocation(asset, excludedLocationId))
    }

    private suspend fun createAssetLocationGroup(asset: Asset, locationGroupId: Long): AssetLocationGroup {
        return assetLocationGroupRepository.save(AssetLocationGroup(asset, locationGroupId))
    }

    @Transactional
    open suspend fun deleteAssetLocations(asset: Asset, locationIds: List<Long>): Long {
        return if (locationIds.isNotEmpty()) {
            assetLocationRepository.deleteByAssetAndLocationIdInList(asset, locationIds)
        } else {
            0
        }
    }

    @Transactional
    open suspend fun deleteAssetBusinesses(asset: Asset, businessIds: List<Long>): Long {
        return if (businessIds.isNotEmpty()) {
            assetBusinessRepository.deleteByAssetAndBusinessIdInList(asset, businessIds)
        } else {
            0
        }
    }

    @Transactional
    open suspend fun deleteAssetExcludedLocations(asset: Asset, excludedLocationIds: List<Long>): Long {
        return if (excludedLocationIds.isNotEmpty()) {
            assetExcludedLocationRepository.deleteByAssetAndExcludedLocationIdInList(asset, excludedLocationIds)
        } else {
            0
        }
    }

    @Transactional
    open suspend fun deleteAssetLocationGroups(asset: Asset, locationGroupIds: List<Long>): Long {
        return if (locationGroupIds.isNotEmpty()) {
            assetLocationGroupRepository.deleteByAssetAndLocationGroupIdInList(asset, locationGroupIds)
        } else {
            0
        }
    }

    /**
     * If a user does not manage ALL the businesses or locations of the asset, they cannot update any resources
     */
    private suspend fun getResourcesToUpdate(
            asset: Asset,
            selectedManagedResources: ApplicableManagedResources,
            managedResources: ApplicableManagedResources
    ): com.transactional.dam.model.AssetResources {
        if (!managedResources.allSalesPartnerResources && managedResources.locationIds.isEmpty() && managedResources.businessIds.isEmpty() && managedResources.locationGroupIds.isEmpty()) {
            throw InvalidRequestException("No managed resources found: managedResources is empty")
        }

        if (selectedManagedResources.locationIds.isEmpty() && selectedManagedResources.businessIds.isEmpty() && selectedManagedResources.locationGroupIds.isEmpty()) {
            throw InvalidRequestException("No resources found for asset association: selectedManagedResources is empty")
        }

        val locationsToDelete =
            asset.locations.mapNotNull { it.locationId } - selectedManagedResources.locationIds.toSet()
        deleteAssetLocations(asset, locationsToDelete)

        val businessesToDelete =
            asset.businesses.mapNotNull { it.businessId } - selectedManagedResources.businessIds.toSet()
        deleteAssetBusinesses(asset, businessesToDelete)

        val excludedLocationsToDelete =
            asset.excludedLocations.mapNotNull { it.excludedLocationId } - selectedManagedResources.excludedLocationIds.toSet()
        deleteAssetExcludedLocations(asset, excludedLocationsToDelete)

        val locationGroupsToDelete =
            asset.locationGroups.mapNotNull { it.locationGroupId } - selectedManagedResources.locationGroupIds.toSet()
        deleteAssetLocationGroups(asset, locationGroupsToDelete)

        return com.transactional.dam.model.AssetResources(
                locations = updateLocations(asset, selectedManagedResources),
                businesses = updateBusinesses(asset, selectedManagedResources),
                excludedLocations = updateExcludedLocations(asset, selectedManagedResources),
                locationGroups = updateLocationGroups(asset, selectedManagedResources)
        )
    }

    private fun populateAssetLocations(assetPage: Page<Asset>, managedResources: ApplicableManagedResources) {
        for (asset in assetPage) {
            asset.locations = getExistingManagedAssetLocations(asset, managedResources)
        }
    }

    private fun populateAssetResources(assetPage: Page<Asset>, managedResources: ApplicableManagedResources) {
        for (asset in assetPage) {
            val assetResources = getExistingAssetResources(asset, managedResources)
            asset.businesses = assetResources.businesses
            asset.locations = assetResources.locations
            asset.excludedLocations = assetResources.excludedLocations
        }
    }

    private fun getExistingManagedAssetLocations(
            asset: Asset,
            managedResources: ApplicableManagedResources
    ): Set<AssetLocation> {
        return if (managedResources.allSalesPartnerResources) {
            asset.locations
        } else {
            filterForManagedLocations(asset, managedResources)
        }
    }

    private fun filterForManagedLocations(
            asset: Asset,
            managedResources: ApplicableManagedResources
    ): Set<AssetLocation> {
        return asset.locations.filter { it.locationId in managedResources.locationIds }.toSet()
    }

    private fun getExistingAssetResources(asset: Asset, managedResources: ApplicableManagedResources): com.transactional.dam.model.AssetResources {
        return if (managedResources.allSalesPartnerResources) {
            com.transactional.dam.model.AssetResources().apply {
                businesses = asset.businesses
                locations = asset.locations
                excludedLocations = asset.excludedLocations
                locationGroups = asset.locationGroups
            }
        } else {
            filterForManagedResources(asset, managedResources)
        }
    }

    private fun filterForManagedResources(
            asset: Asset,
            managedResources: ApplicableManagedResources
    ): com.transactional.dam.model.AssetResources {
        return com.transactional.dam.model.AssetResources().apply {
            businesses = asset.businesses
                .filter { it.businessId in managedResources.businessIds }.toSet()
            locations = asset.locations
                .filter { it.locationId in managedResources.locationIds }.toSet()
            excludedLocations = asset.excludedLocations
                .filter { it.excludedLocationId in managedResources.locationIds }.toSet()
            locationGroups = asset.locationGroups
                .filter { it.locationGroupId in managedResources.locationGroupIds }.toSet()
        }
    }
}
