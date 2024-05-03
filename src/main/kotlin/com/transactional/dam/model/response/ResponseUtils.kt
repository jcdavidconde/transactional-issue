package com.transactional.dam.model.response

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import com.transactional.dam.http.monolith.model.EnrichedAssetData
import com.transactional.dam.model.SocialPostTemplate
import com.transactional.dam.model.request.ABEListAssetRequest
import com.transactional.dam.model.request.ABEListFolderRequest
import io.micronaut.data.model.Page

object ResponseUtils {

    fun Page<Asset>.toEnrichedResponse(
        enrichedAssets: List<EnrichedAssetData>
    ): com.transactional.dam.model.response.ListEnrichedAssetResponse {
        val assets = content.mapNotNull { asset ->
            enrichedAssets.find { enrichedAsset ->
                enrichedAsset.templateId == asset.templateId && enrichedAsset.assetId == asset.id
            }?.let { matchedTemplate ->
                asset.toEnrichedResponse(matchedTemplate)
            }
        }
        return com.transactional.dam.model.response.ListEnrichedAssetResponse(assets, size, pageNumber, totalSize)
    }

    fun Page<Asset>.toABEResponse(request: ABEListAssetRequest): com.transactional.dam.model.response.ABEListAssetResponse {
        val assets = com.transactional.dam.model.response.ResponseUtils.getRequestedItems(request.offset, request.max, this)
        val assetResponses = assets.map { it.toABEResponse() }
        return com.transactional.dam.model.response.ABEListAssetResponse(assetResponses, request.offset, request.max, totalSize)
    }

    fun Asset.toResponse(): com.transactional.dam.model.response.AssetResponse {
        return com.transactional.dam.model.response.AssetResponse(
                id,
                name,
                description,
                dateCreated,
                dateUpdated,
                type,
                startDate,
                endDate,
                status,
                authorId,
                folder?.id,
                templateId,
                locations.map { it.locationId }.toSet(),
                businesses.map { it.businessId }.toSet(),
                excludedLocations.map { it.excludedLocationId }.toSet(),
                locationGroups.map { it.locationGroupId }.toSet()
        )
    }

    fun Asset.toABEResponse(): com.transactional.dam.model.response.ABEAssetResponse {
        return com.transactional.dam.model.response.ABEAssetResponse(
                id,
                name,
                description,
                dateCreated,
                dateUpdated,
                type,
                startDate,
                endDate,
                status,
                authorId,
                folder?.id,
                templateId
        )
    }

    fun Asset.toEnrichedResponse(
        templateInfo: EnrichedAssetData
    ): com.transactional.dam.model.response.EnrichedAssetResponse {
        return com.transactional.dam.model.response.EnrichedAssetResponse(
                id = id,
                name = name,
                description = description,
                dateCreated = dateCreated,
                dateUpdated = dateUpdated,
                type = type,
                locationIds = locations.map { it.locationId }.toSet(),
                businessIds = businesses.map { it.businessId }.toSet(),
                excludedLocationIds = excludedLocations.map { it.excludedLocationId }.toSet(),
                locationGroupIds = locationGroups.map { it.locationGroupId }.toSet(),
                startDate = startDate,
                endDate = endDate,
                status = status,
                folderId = folder?.id,
                template = SocialPostTemplate(
                        id = templateId,
                        isStoreLocator = templateInfo.isStoreLocator,
                        postType = templateInfo.postType,
                        author = templateInfo.author,
                        directories = templateInfo.directories,
                        photos = templateInfo.photos,
                        videos = templateInfo.videos
                )
        )
    }

    fun Page<Folder>.toABEResponse(request: ABEListFolderRequest): com.transactional.dam.model.response.ABEListFolderResponse {
        val folders = com.transactional.dam.model.response.ResponseUtils.getRequestedItems(request.offset, request.max, this)
        val folderResponses = folders.map { it.toABEResponse() }
        return com.transactional.dam.model.response.ABEListFolderResponse(folderResponses, request.offset, request.max, totalSize)
    }

    fun Folder.toABEResponse(): com.transactional.dam.model.response.ABEFolderResponse {
        return com.transactional.dam.model.response.ABEFolderResponse(id, name, description, dateCreated, dateUpdated, status, type, authorId)
    }

    fun Folder.toResponse(numVisibleAssets: Long, numTotalAssets: Long): com.transactional.dam.model.response.FolderResponse {
        return com.transactional.dam.model.response.FolderResponse(
                id,
                name,
                description,
                dateCreated,
                dateUpdated,
                status,
                type,
                authorId,
                numVisibleAssets,
                numTotalAssets
        )
    }

    fun Folder.toResponse(): com.transactional.dam.model.response.FolderResponse {
        return com.transactional.dam.model.response.FolderResponse(
                id,
                name,
                description,
                dateCreated,
                dateUpdated,
                status,
                type,
                authorId
        )
    }

    private fun <I> getRequestedItems(requestOffset: Int, requestMax: Int, page: Page<I>): List<I> {
        val startIndexOnPage = requestOffset % page.size
        var items = page.content.drop(startIndexOnPage)
        if (items.size > requestMax) {
            items = items.dropLast(items.size - requestMax)
        }
        return items
    }
}
