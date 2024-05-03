package com.transactional.dam.service

import com.transactional.dam.domain.Asset
import com.transactional.dam.http.monolith.model.AssetData
import com.transactional.dam.http.monolith.model.EnrichAssetDataRequest
import com.transactional.dam.http.monolith.model.EnrichedAssetData
import com.transactional.dam.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class SocialTemplateService(
    @Inject private val monolithClient: com.transactional.dam.http.monolith.MonolithClient
) {

    suspend fun deleteTemplate(asset: Asset, user: User) {
        monolithClient.deleteTemplate(user, asset.templateId)
    }

    suspend fun enrichAssetData(assets: List<Asset>, user: User): List<EnrichedAssetData> {
        val enrichAssetDataRequest = prepareAssetData(assets)
        if (enrichAssetDataRequest.enrichDataModels.isEmpty()) {
            return emptyList()
        }
        return monolithClient.enrichAssetData(user, enrichAssetDataRequest).enrichDataModels
    }

    private fun prepareAssetData(assets: List<Asset>): EnrichAssetDataRequest {
        val enrichAssetDataRequest = assets.map {
            AssetData(
                assetId = it.id,
                authorId = it.authorId,
                templateId = it.templateId
            )
        }

        return EnrichAssetDataRequest(enrichAssetDataRequest)
    }
}
