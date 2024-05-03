package com.transactional.dam.model

import com.transactional.dam.domain.AssetBusiness
import com.transactional.dam.domain.AssetExcludedLocation
import com.transactional.dam.domain.AssetLocation
import com.transactional.dam.domain.AssetLocationGroup
import io.micronaut.core.annotation.Introspected

@Introspected
data class AssetResources(
        var businesses: Set<AssetBusiness> = emptySet(),
        var locations: Set<AssetLocation> = emptySet(),
        var excludedLocations: Set<AssetExcludedLocation> = emptySet(),
        var locationGroups: Set<AssetLocationGroup> = emptySet()
)
