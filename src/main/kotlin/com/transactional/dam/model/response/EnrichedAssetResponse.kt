package com.transactional.dam.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.transactional.dam.domain.Asset
import com.transactional.dam.model.SocialPostTemplate
import java.time.LocalDateTime

data class EnrichedAssetResponse(

        val id: Long,

        val name: String,

        val description: String?,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateCreated: LocalDateTime,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateUpdated: LocalDateTime,

        val type: Asset.Type,

        val locationIds: Set<Long?>?,

        val businessIds: Set<Long?>?,

        val excludedLocationIds: Set<Long?>?,

        val locationGroupIds: Set<Long?>?,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val startDate: LocalDateTime,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val endDate: LocalDateTime? = null,

        val status: Asset.Status,

        val folderId: Long?,

        val template: SocialPostTemplate
)
