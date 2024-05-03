package com.transactional.dam.model.request

import java.time.LocalDate

data class DeactivateAssetRequest(
    val endDate: LocalDate
)
