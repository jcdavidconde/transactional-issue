package com.transactional.dam.task

import com.transactional.dam.domain.Asset
import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import com.transactional.dam.service.AssetService
import io.micronaut.data.model.Pageable
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AssetObsoleteLocationLinksDeletionTask(
        private val assetService: AssetService,
        private val monolithClient: com.transactional.dam.http.monolith.MonolithClient,
        private val assetPageSize: Int,
        private val salesPartnerIds: Set<Long>? = null
) : Runnable {

    override fun run() {
        runBlocking {
            LOG.info(
                "Asset obsolete location links deletion task - starting for salesPartnerIds: {}",
                salesPartnerIds ?: "all"
            )

            val spIds = salesPartnerIds ?: assetService.getAllSalesPartnerIds()
            for (spId in spIds) {
                deleteObsoleteLocationLinks(spId)
            }

            LOG.info(
                "Asset obsolete location links deletion task - finished for salesPartnerIds: {}",
                salesPartnerIds ?: "all (${spIds.size})"
            )
        }
    }

    private suspend fun deleteObsoleteLocationLinks(salesPartnerId: Long) {
        val validLocationIds =
            monolithClient.getLocationIds(salesPartnerId, ManagedResourcesSelection(allManagedResources = true))

        var numAssetsProcessed = 0
        var page = 0
        do {
            val pageable = Pageable.from(page++, assetPageSize)
            val assets = assetService.listAssets(salesPartnerId, pageable)
            for (asset in assets) {
                deleteObsoleteLocationLinks(asset, validLocationIds)
            }
            numAssetsProcessed += assets.content.size
        } while (numAssetsProcessed < assets.totalSize)

        LOG.debug(
            "Asset obsolete location links deletion task - salesPartnerId: {} numAssetsProcessed: {}",
            salesPartnerId,
            numAssetsProcessed
        )
    }

    private suspend fun deleteObsoleteLocationLinks(asset: Asset, validLocationIds: List<Long>) {
        val existingAssetLocationIds = asset.locations.map { it.locationId!! }
        val obsoleteLocationIds = existingAssetLocationIds - validLocationIds.toSet()
        val numDeleted = assetService.deleteAssetLocations(asset, obsoleteLocationIds)

        if (numDeleted > 0) {
            LOG.info(
                "Asset obsolete location links deletion task - asset.id: {} numDeleted: {}",
                asset.id,
                numDeleted
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AssetObsoleteLocationLinksDeletionTask::class.java)
    }
}
