package com.transactional.dam.model.request

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.QueryValue

@Introspected
data class ListAssetRequest(
        @field:QueryValue val type: Asset.Type?,
        @field:QueryValue("status") val status: Asset.Status?,
        @field:QueryValue("query") val query: String?,
        @field:QueryValue("folder_ids") val folderIds: List<Long>?,
        @field:QueryValue("folder_statuses") val folderStatuses: List<Folder.Status>?,
        @field:QueryValue("location_ids") val locationIds: List<Long>?,
        @field:QueryValue("business_ids") val businessIds: List<Long>?,
        @field:QueryValue(defaultValue = "100") val size: Int,
        @field:QueryValue(defaultValue = "0") val page: Int
)
