package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetLocationGroup
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import jakarta.persistence.Id

@Repository
interface AssetLocationGroupRepository : CoroutineCrudRepository<AssetLocationGroup, Id> {

    suspend fun deleteByAssetAndLocationGroupIdInList(asset: Asset, locationGroupIds: List<Long>): Long
}
