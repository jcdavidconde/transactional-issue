package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.model.AssetsCounts
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface AssetRepository : CoroutineCrudRepository<Asset, Long> {

    suspend fun findByIdAndStatusNotEqual(id: Long, status: Asset.Status): Asset?

    @Query(
        """select templateId from Asset where templateId in (:templateIds) """
    )
    suspend fun findIdsByTemplateIdInList(templateIds: List<Long>): List<Long>

    @Query(
        """UPDATE Asset a
                SET a.dateUpdated = :dateUpdated
                WHERE a.id = :id"""
    )
    suspend fun updateDateUpdatedById(
        id: Long,
        dateUpdated: LocalDateTime
    ): Long

    @Query(
        """UPDATE Asset a
            SET a.status = :newStatus, a.dateUpdated = :dateUpdated
            WHERE CAST(a.startDate as LocalDate) = :startDate
            AND a.status = :oldStatus
        """
    )
    suspend fun updateStatusAndDateUpdatedByStartDateAndStatus(
            newStatus: Asset.Status,
            dateUpdated: LocalDateTime,
            startDate: LocalDate,
            oldStatus: Asset.Status
    ): Long

    @Query(
        """UPDATE Asset a
            SET a.status = :newStatus, a.dateUpdated = :dateUpdated
            WHERE CAST(a.endDate as LocalDate) = :endDate
            AND a.status = :oldStatus
        """
    )
    suspend fun updateStatusAndDateUpdatedByEndDateAndStatus(
            newStatus: Asset.Status,
            dateUpdated: LocalDateTime,
            endDate: LocalDate,
            oldStatus: Asset.Status
    ): Long

    @Query(
        """UPDATE Asset a
            SET a.status = :status, a.dateUpdated = :dateUpdated
            WHERE a.folder.id = :folderId
            AND a.status <> :status
        """
    )
    suspend fun updateStatusAndDateUpdatedByFolderIdAndStatusNotAlreadyEqual(
            folderId: Long,
            status: Asset.Status,
            dateUpdated: LocalDateTime
    ): Long

    suspend fun countByFolderIdAndStatusIn(
        folderId: Long,
        statuses: List<Asset.Status>
    ): Long

    @Query(
        """SELECT DISTINCT a.* FROM asset a
            WHERE a.sales_partner_id = :salesPartnerId
            AND a.status IN (:statuses)
            AND a.folder_id = :folderId
            AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
            ORDER BY a.date_updated DESC""",
        countQuery = """SELECT COUNT(DISTINCT a.id) 
                        FROM asset a
                        WHERE a.sales_partner_id = :salesPartnerId
                        AND a.status IN (:statuses)
                        AND a.folder_id = :folderId
                        AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))""",
        nativeQuery = true
    )
    suspend fun findAssetsByFolderIdAndSalesPartner(
        folderId: Long,
        statuses: List<String>,
        salesPartnerId: Long,
        query: String?,
        pageable: Pageable
    ): Page<Asset>

    suspend fun findBySalesPartnerIdAndStatusIn(
            salesPartnerId: Long,
            statuses: List<Asset.Status>,
            pageable: Pageable
    ): Page<Asset>

    suspend fun findDistinctSalesPartnerIdByStatusIn(statuses: List<Asset.Status>): Set<Long>

    @Query(
        """SELECT COUNT (DISTINCT (CASE WHEN a.status = 'VISIBLE' THEN a.id END)) AS visible,
            COUNT (DISTINCT (CASE WHEN a.status <> 'REMOVED' THEN a.id END)) AS total
            FROM Asset a
            WHERE a.folder.id = :folderId
            AND a.salesPartnerId = (:salesPartnerId)"""
    )
    suspend fun countVisibleAndTotalByFolderIdAndSalesPartnerId(
        folderId: Long,
        salesPartnerId: Long
    ): AssetsCounts

    @Query(
        """SELECT a.*
            FROM asset a
                     JOIN folder f on f.id = a.folder_id
            WHERE a.sales_partner_id = :salesPartnerId
              AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
              AND a.status IN (:statuses)
              AND f.status IN (:folderStatuses)
              AND a.type = :type
              AND (COALESCE(:folderIds) IS NULL OR f.id IN (:folderIds))
            GROUP BY a.id, a.date_updated
            ORDER BY a.date_updated DESC""",
        countQuery = """SELECT COUNT(DISTINCT a.id)
                        FROM asset a
                        JOIN folder f on f.id = a.folder_id
                        WHERE a.sales_partner_id = :salesPartnerId
                          AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
                          AND a.status IN (:statuses)
                          AND f.status IN (:folderStatuses)
                          AND a.type = :type
                          AND (COALESCE(:folderIds) IS NULL OR f.id IN (:folderIds))
                       """,
        nativeQuery = true
    )
    suspend fun findAssetsByTypeAndSalesPartner(
        type: String,
        query: String?,
        folderIds: List<Long>?,
        statuses: List<String>,
        folderStatuses: List<String>,
        salesPartnerId: Long,
        pageable: Pageable
    ): Page<Asset>

    @Query(
        """SELECT a.*
            FROM asset a
                     JOIN folder f on f.id = a.folder_id
                     LEFT JOIN asset_location al on a.id = al.asset_id
                     LEFT JOIN asset_business ab on a.id = ab.asset_id
                     LEFT JOIN asset_location_group alg on a.id = alg.asset_id
                     LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                                FROM asset_excluded_location
                                WHERE excluded_location_id in (:locations)
                                GROUP BY asset_id) ael on a.id = ael.asset_id
            WHERE a.sales_partner_id = :salesPartnerId
              AND a.status IN (:statuses)
              AND f.status IN (:folderStatuses)
              AND a.type = :type
              AND (COALESCE(:folderIds) IS NULL OR f.id IN (:folderIds))
              AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
              AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroupIds))
              AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)
            GROUP BY a.id, a.date_updated
            ORDER BY a.date_updated DESC""",
        countQuery = """SELECT COUNT(DISTINCT a.id)
                        FROM asset a
                                 JOIN folder f on f.id = a.folder_id
                                 LEFT JOIN asset_location al on a.id = al.asset_id
                                 LEFT JOIN asset_business ab on a.id = ab.asset_id
                                 LEFT JOIN asset_location_group alg on a.id = alg.asset_id
                                 LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                                            FROM asset_excluded_location
                                            WHERE excluded_location_id in (:locations)
                                            GROUP BY asset_id) ael on a.id = ael.asset_id
                        WHERE a.sales_partner_id = :salesPartnerId
                          AND a.status IN (:statuses)
                          AND f.status IN (:folderStatuses)
                          AND a.type = :type
                          AND (COALESCE(:folderIds) IS NULL OR f.id IN (:folderIds))
                          AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
                          AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroupIds))
                          AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)""",
        nativeQuery = true
    )
    suspend fun findAssetsByTypeAndResources(
        type: String,
        query: String?,
        folderIds: List<Long>?,
        statuses: List<String>,
        folderStatuses: List<String>,
        locations: List<Long>,
        businesses: List<Long>,
        locationsSize: Int,
        locationGroupIds: List<Long>,
        salesPartnerId: Long,
        pageable: Pageable
    ): Page<Asset>

    @Query(
        """SELECT a.*
            FROM asset a
                     LEFT JOIN asset_location al on a.id = al.asset_id
                     LEFT JOIN asset_business ab on a.id = ab.asset_id
                     LEFT JOIN asset_location_group alg on a.id = alg.asset_id
                     LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                                FROM asset_excluded_location
                                WHERE excluded_location_id in (:locations)
                                GROUP BY asset_id) ael on a.id = ael.asset_id
            WHERE a.folder_id = :folderId
              AND a.sales_partner_id = :salesPartnerId
              AND a.status IN (:statuses)
              AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroups))
              AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)
              AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
            GROUP BY a.id, a.date_updated
            ORDER BY a.date_updated DESC""",
        countQuery = """SELECT COUNT(DISTINCT a.id)
                        FROM asset a
                                 LEFT JOIN asset_location al on a.id = al.asset_id
                                 LEFT JOIN asset_business ab on a.id = ab.asset_id
                                 LEFT JOIN asset_location_group alg on a.id = alg.asset_id
                                 LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                                            FROM asset_excluded_location
                                            WHERE excluded_location_id in (:locations)
                                            GROUP BY asset_id) ael on a.id = ael.asset_id
                        WHERE a.folder_id = :folderId
                          AND a.sales_partner_id = :salesPartnerId
                          AND a.status IN (:statuses)
                          AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroups))
                          AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)
                          AND (:query IS NULL OR :query = '' OR MATCH(a.name, a.description) AGAINST (:query IN BOOLEAN MODE))
                        ORDER BY a.date_updated DESC""",
        nativeQuery = true
    )
    suspend fun findAssetsByFolderIdAndResources(
        folderId: Long,
        statuses: List<String>,
        locations: List<Long>?,
        businesses: List<Long>?,
        locationsSize: Int,
        locationGroups: List<Long>?,
        query: String?,
        salesPartnerId: Long,
        pageable: Pageable
    ): Page<Asset>

    @Query(
        """
            SELECT COUNT(DISTINCT a.id)
            FROM asset a    
                LEFT JOIN asset_business ab ON a.id = ab.asset_id
                LEFT JOIN asset_location al ON a.id = al.asset_id
                LEFT JOIN asset_location_group alg ON a.id = alg.asset_id
        LEFT JOIN (
            SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count 
            FROM asset_excluded_location 
            WHERE excluded_location_id in (:locations) 
            GROUP BY asset_id
        ) ael on a.id = ael.asset_id 
        WHERE a.folder_id = :folderId
            AND a.sales_partner_id = :salesPartnerId
            AND a.status IN (:statuses)
            AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroupIds))
            AND (ael.excluded_count is null OR ael.excluded_count < :size)""",
        nativeQuery = true
    )
    suspend fun countAssets(
        folderId: Long,
        statuses: List<String>,
        businesses: List<Long>,
        locations: List<Long>,
        size: Int,
        locationGroupIds: List<Long>,
        salesPartnerId: Long
    ): Long

    @Query(
        """SELECT COUNT(DISTINCT (CASE WHEN a.status = 'VISIBLE' THEN a.id END))  AS visible,
                  COUNT(DISTINCT (CASE WHEN a.status <> 'REMOVED' THEN a.id END)) AS total
           FROM asset a
                 LEFT JOIN asset_business ab ON a.id = ab.asset_id
                 LEFT JOIN asset_location al ON a.id = al.asset_id
                 LEFT JOIN asset_location_group alg ON a.id = alg.asset_id
                 LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                            FROM asset_excluded_location
                            WHERE excluded_location_id in (:locations)
                            GROUP BY asset_id) ael on a.id = ael.asset_id
        WHERE a.folder_id = :folderId
          AND a.sales_partner_id = :salesPartnerId
          AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroupIds))
          AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)""",
        nativeQuery = true
    )
    suspend fun countVisibleAndTotalAssets(
        folderId: Long,
        locations: List<Long>,
        businesses: List<Long>,
        locationsSize: Int,
        locationGroupIds: List<Long>,
        salesPartnerId: Long
    ): AssetsCounts
}
