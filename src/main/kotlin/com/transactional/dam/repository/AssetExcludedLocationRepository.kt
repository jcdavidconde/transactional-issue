package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetExcludedLocation
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import jakarta.persistence.Id

@Repository
interface AssetExcludedLocationRepository : CoroutineCrudRepository<AssetExcludedLocation, Id> {

    suspend fun deleteByAssetAndExcludedLocationIdInList(asset: Asset, excludedLocationIds: List<Long>): Long
}
