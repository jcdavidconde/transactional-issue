package com.transactional.dam.task

import com.transactional.dam.service.AssetService
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AssetDeactivationTask(private val assetService: AssetService, private val assetEndDate: LocalDate) : Runnable {
    override fun run() {
        runBlocking {
            LOG.info("Asset deactivation task for {} starting", assetEndDate)

            val numDeactivated = assetService.deactivateAssets(assetEndDate)

            LOG.info(
                "Asset deactivation task for {} finished - number of assets deactivated: {}",
                assetEndDate,
                numDeactivated
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AssetDeactivationTask::class.java)
    }
}
