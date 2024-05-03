package com.transactional.dam.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.transactional.dam.domain.Asset
import java.time.LocalDateTime

data class ABEAssetResponse(

        val id: Long,

        val name: String,

        val description: String?,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateCreated: LocalDateTime,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateUpdated: LocalDateTime,

        val type: Asset.Type,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val startDate: LocalDateTime,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val endDate: LocalDateTime? = null,

        val status: Asset.Status,

        val authorId: Long,

        val folderId: Long?,

        val templateId: Long
)
