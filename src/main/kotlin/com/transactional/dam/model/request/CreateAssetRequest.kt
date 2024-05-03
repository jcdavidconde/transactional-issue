package com.transactional.dam.model.request

import com.transactional.dam.domain.Asset
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateAssetRequest(

        @field: [NotBlank Size(min = 1, max = 250)]
    val name: String,

        @field: [Size(max = 4096)]
    val description: String?,

        val type: Asset.Type,

        val status: Asset.Status = Asset.Status.VISIBLE,

        val startDate: LocalDateTime = LocalDateTime.now(),

        val endDate: LocalDateTime? = null,

        val allManagedResources: Boolean = false,

        val labels: List<String>? = null,

        val businessIds: List<Long>? = null,

        val locationIds: List<Long>? = null,

        val excludedLocationIds: List<Long>? = null,

        val locationGroupIds: List<Long>? = null,

        @field: [NotNull Positive]
    val authorId: Long,

        @field: [NotNull Positive]
    val folderId: Long,

        @field: [NotNull]
    val templateId: Long = 0 // default value to be removed once FE is sending the template id (NMA-2167)
)
