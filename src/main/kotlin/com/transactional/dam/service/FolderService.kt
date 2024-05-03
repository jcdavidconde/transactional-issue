package com.transactional.dam.service

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import com.transactional.dam.domain.Folder.Status
import com.transactional.dam.model.ApplicableManagedResources
import com.transactional.dam.model.User
import com.transactional.dam.model.request.CreateFolderRequest
import com.transactional.dam.model.request.ListFolderRequest
import com.transactional.dam.repository.AssetRepository
import com.transactional.dam.repository.FolderRepository
import com.transactional.dam.service.exception.NotFoundException
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.transaction.annotation.ReadOnly
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.hibernate.Hibernate
import java.time.LocalDateTime

@Singleton
open class FolderService(
        @Inject private val folderRepository: FolderRepository,
        @Inject private val assetRepository: AssetRepository
) {

    @Transactional
    open suspend fun createFolder(request: CreateFolderRequest): Folder {
        val folder = Folder.create(request)
        return folderRepository.save(folder)
    }

    @ReadOnly
    open suspend fun getFolder(id: Long): Folder {
        val folder = folderRepository.findByIdAndStatusNotEqual(id, Status.REMOVED)
            ?: throw NotFoundException("Folder", id)
        Hibernate.initialize(folder.assets)
        return folder
    }

    @ReadOnly
    open suspend fun listFolders(
        user: User,
        request: ListFolderRequest,
        managedResources: ApplicableManagedResources
    ): List<Folder> {
        val folderStatuses = request.statuses ?: Status.nonRemoved

        // If asset statuses are not specified, empty folders (as it appears to the user) can be returned:
        //  - folders containing only REMOVED assets when user is ADMIN or the folder's author
        //  - folders not containing any asset managed by the user when the user is the folder's author
        // If asset statuses are specified, no empty folders (as it appears to the user) can be returned since:
        //  - folder is returned only if it contains asset(s) matching the specified statuses (REMOVED cannot be specified)
        //  - status of only the assets managed by user are checked; so, folder must have assets managed by the user
        return if (request.assetStatuses.isNullOrEmpty()) {
            listFolders(user, request.type, folderStatuses, managedResources)
        } else {
            listFolders(request.type, folderStatuses, request.assetStatuses, managedResources)
        }
    }

    private suspend fun listFolders(
            user: User,
            type: Folder.Type,
            folderStatuses: List<Status>,
            managedResources: ApplicableManagedResources
    ): List<Folder> {
        return if (managedResources.allSalesPartnerResources) {
            folderRepository.findFoldersByAuthorOrSalesPartner(
                type,
                folderStatuses,
                user.id,
                managedResources.salesPartnerId
            )
        } else {
            // An empty folder (as it appears to the user) should not be returned to a user if they are not the
            // folder's author. So, check if the folder has an asset with non-removed status managed by user
            folderRepository.findFoldersByAuthorOrAssetResources(
                type.toString(),
                folderStatuses.map { it.toString() },
                Asset.Status.nonRemoved.map { it.toString() },
                user.id,
                managedResources.locationIds,
                managedResources.businessIds,
                managedResources.locationIds.size,
                managedResources.locationGroupIds,
                managedResources.salesPartnerId
            )
        }
    }

    @Transactional
    open suspend fun findByNameAndAuthorId(name: String, authorId: Long): Folder? =
        folderRepository.findByNameAndAuthorId(name, authorId)

    private suspend fun listFolders(
            type: Folder.Type,
            folderStatuses: List<Status>,
            assetStatuses: List<Asset.Status>,
            managedResources: ApplicableManagedResources
    ): List<Folder> {
        return if (managedResources.allSalesPartnerResources) {
            folderRepository.findFoldersByAssetStatusAndSalesPartner(
                type,
                folderStatuses,
                assetStatuses,
                managedResources.salesPartnerId
            )
        } else {
            folderRepository.findFoldersByAssetStatusAndAssetResources(
                type.toString(),
                folderStatuses.map { it.toString() },
                assetStatuses.map { it.toString() },
                managedResources.locationIds,
                managedResources.businessIds,
                managedResources.locationIds.size,
                managedResources.locationGroupIds,
                managedResources.salesPartnerId
            )
        }
    }

    @ReadOnly
    open suspend fun listFolders(
        locationIds: List<Long>?,
        businessId: Long,
        locationGroupIds: List<Long>?,
        salesPartnerId: Long,
        pageable: Pageable
    ): Page<Folder> {
        return folderRepository.findFoldersByAssetResources(
            Status.nonRemoved,
            locationIds,
            listOf(businessId),
            locationGroupIds,
            salesPartnerId,
            pageable
        )
    }

    @Transactional
    open suspend fun updateFolder(id: Long, updateFolderRequest: com.transactional.dam.model.request.UpdateFolderRequest): Folder {
        val folder = folderRepository.findByIdAndStatusNotEqual(id, Folder.Status.REMOVED)
            ?: throw NotFoundException("Folder", id)

        folder.name = updateFolderRequest.name ?: folder.name
        folder.description = updateFolderRequest.description
        folder.status = updateFolderRequest.status ?: folder.status

        return folderRepository.update(folder)
    }

    @Transactional
    open suspend fun deleteFolder(id: Long) {
        val folder = folderRepository.findByIdAndStatusNotEqual(id, Status.REMOVED)
            ?: throw NotFoundException("Folder", id)

        assetRepository.updateStatusAndDateUpdatedByFolderIdAndStatusNotAlreadyEqual(
            id,
            Asset.Status.REMOVED,
            LocalDateTime.now()
        )

        folder.status = Status.REMOVED
        folderRepository.update(folder)
    }

    @ReadOnly
    open suspend fun getTotalCountOfAssetsForFolder(folderId: Long): Long {
        return assetRepository.countByFolderIdAndStatusIn(
            folderId,
            Asset.Status.nonRemoved
        )
    }

    @ReadOnly
    open suspend fun getUserCountOfAssetsForFolder(folderId: Long, managedResources: ApplicableManagedResources): Long {
        return assetRepository.countAssets(
            folderId,
            Asset.Status.nonRemoved.map { it.toString() },
            managedResources.businessIds,
            managedResources.locationIds,
            managedResources.locationIds.size,
            managedResources.locationGroupIds,
            managedResources.salesPartnerId
        )
    }
}
