package com.transactional.dam.http.monolith.model

import com.transactional.dam.model.Author

data class EnrichAssetDataResponse(
    val enrichDataModels: List<EnrichedAssetData>
)

data class EnrichedAssetData(
        val assetId: Long,
        val templateId: Long,
        val isStoreLocator: Boolean?,
        val postType: String?,
        val author: com.transactional.dam.model.Author?,
        val directories: List<String>?,
        val photos: List<String>?,
        val videos: List<String>?
)
