package com.transactional.dam.model.request

data class DeleteAssetObsoleteLinksRequest(
    val salesPartnerIds: List<Long>? = null
)
