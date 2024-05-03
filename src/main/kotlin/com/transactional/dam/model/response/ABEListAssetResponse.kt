package com.transactional.dam.model.response

data class ABEListAssetResponse(
    val assets: List<ABEAssetResponse>,
    val offset: Int,
    val max: Int,
    val totalCount: Long
)
