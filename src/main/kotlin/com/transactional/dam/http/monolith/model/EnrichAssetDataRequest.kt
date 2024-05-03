package com.transactional.dam.http.monolith.model

import io.micronaut.core.annotation.Introspected

@Introspected
data class EnrichAssetDataRequest(
    val enrichDataModels: List<AssetData>
)

data class AssetData(
    val assetId: Long,
    val authorId: Long,
    val templateId: Long
)
