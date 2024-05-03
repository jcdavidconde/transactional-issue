package com.transactional.dam.model.request

import com.transactional.dam.domain.Asset
import java.time.LocalDateTime
import jakarta.validation.constraints.Size

data class UpdateAssetRequest(

        @field: [Size(min = 1, max = 250)]
    val name: String? = null,

        @field: [Size(max = 4096)]
    val description: String? = null,

        val status: Asset.Status? = null,

        val startDate: LocalDateTime? = null,

        val endDate: LocalDateTime? = null,

        val allManagedResources: Boolean = false,

        val labels: List<String>? = null,

        val businessIds: List<Long>? = null,

        val locationIds: List<Long>? = null,

        val excludedLocationIds: List<Long>? = null,

        val locationGroupIds: List<Long>? = null,

        val folderId: Long? = null
)
