package com.transactional.dam.model

import io.micronaut.core.annotation.Introspected

@Introspected
data class AssetsCounts(
    var visible: Long,
    var total: Long
)
