package com.transactional.dam.model.response

data class ListEnrichedAssetResponse(val assets: List<EnrichedAssetResponse>, val size: Int, val page: Int, val totalSize: Long)
