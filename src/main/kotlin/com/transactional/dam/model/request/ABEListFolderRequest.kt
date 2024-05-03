package com.transactional.dam.model.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class ABEListFolderRequest(
    val locationIds: List<Long>?,

    @field: [NotNull Positive]
    val businessId: Long,

    val salesPartnerId: Long,

    val locationGroupIds: List<Long>?,

    @field: [PositiveOrZero] val offset: Int = 0,

    @field: [Positive] val max: Int = 10
)
