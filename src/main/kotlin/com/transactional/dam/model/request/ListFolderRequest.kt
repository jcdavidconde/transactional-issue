package com.transactional.dam.model.request

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.QueryValue

@Introspected
data class ListFolderRequest(
        @field:QueryValue val type: Folder.Type,
        @field:QueryValue("statuses") val statuses: List<Folder.Status>?,
        @field:QueryValue("asset_statuses") val assetStatuses: List<Asset.Status>?,
        @field:QueryValue("location_ids") val locationIds: List<Long>?
)
