package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.AssetBusiness
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import jakarta.persistence.Id

@Repository
interface AssetBusinessRepository : CoroutineCrudRepository<AssetBusiness, Id> {

    suspend fun deleteByAssetAndBusinessIdInList(asset: Asset, businessIds: List<Long>): Long
}
