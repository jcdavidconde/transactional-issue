package com.transactional.dam.task

import com.transactional.dam.service.AssetService
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AssetActivationTask(private val assetService: AssetService, private val assetStartDate: LocalDate) : Runnable {

    override fun run() {
        runBlocking {
            LOG.info("Asset activation task for {} starting", assetStartDate)

            val numActivated = assetService.activateAssets(assetStartDate)

            LOG.info(
                "Asset activation task for {} finished - number of assets activated: {}",
                assetStartDate,
                numActivated
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AssetActivationTask::class.java)
    }
}
