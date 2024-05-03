package com.transactional.dam.model.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class ABEListAssetRequest(
    @field: [NotNull Positive]
    val folderId: Long,

    val locationIds: List<Long>?,

    @field: [NotNull Positive]
    val businessId: Long,

    val locationGroupIds: List<Long>?,

    val query: String?,

    val salesPartnerId: Long,

    @field: [PositiveOrZero] val offset: Int = 0,

    @field: [Positive] val max: Int = 10
)
