package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetLocation
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import jakarta.persistence.Id

@Repository
interface AssetLocationRepository : CoroutineCrudRepository<AssetLocation, Id> {

    suspend fun deleteByAssetAndLocationIdInList(asset: Asset, locationIds: List<Long>): Long
}
