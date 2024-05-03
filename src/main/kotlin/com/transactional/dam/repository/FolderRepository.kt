package com.transactional.dam.repository

import com.transactional.dam.domain.Asset
import com.transactional.dam.domain.Folder
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@Repository
interface FolderRepository : CoroutineCrudRepository<Folder, Long> {

    suspend fun findByIdAndStatusNotEqual(id: Long, status: Folder.Status): Folder?

    @SingleResult
    @Query(
        """Select DISTINCT f FROM Folder f
        WHERE f.authorId = :authorId
            AND f.name = :name
        """
    )
    suspend fun findByNameAndAuthorId(name: String, authorId: Long): Folder?

    @Query(
        """Select DISTINCT f
        FROM Folder f LEFT JOIN f.assets a
        WHERE (a.salesPartnerId = :salesPartnerId
        OR f.authorId = :userId)
        AND f.status IN (:statuses)
        AND f.type = :type
        ORDER BY f.name ASC"""
    )
    suspend fun findFoldersByAuthorOrSalesPartner(
            type: Folder.Type,
            statuses: List<Folder.Status>,
            userId: Long,
            salesPartnerId: Long
    ): List<Folder>

    @Query(
        """Select DISTINCT f
        FROM Folder f JOIN f.assets a
        WHERE a.salesPartnerId = :salesPartnerId
        AND f.status IN (:statuses)
        AND a.status IN (:assetStatuses)
        AND f.type = :type
        ORDER BY f.dateCreated DESC"""
    )
    suspend fun findFoldersByAssetStatusAndSalesPartner(
            type: Folder.Type,
            statuses: List<Folder.Status>,
            assetStatuses: List<Asset.Status>,
            salesPartnerId: Long
    ): List<Folder>

    @Query(
        """SELECT DISTINCT f.*
            FROM folder f
            WHERE f.status IN (:statuses)
              AND f.type = :type
              AND f.author_id = :userId
            
            UNION
            
            SELECT f.*
            FROM folder f
                     JOIN asset a ON f.id = a.folder_id
                     LEFT JOIN asset_location al ON a.id = al.asset_id
                     LEFT JOIN asset_business ab ON a.id = ab.asset_id
                     LEFT JOIN asset_location_group alg ON a.id = alg.asset_id
            WHERE f.status IN (:statuses)
              AND f.type = :type
              AND a.sales_partner_id = :salesPartnerId
              AND a.status IN (:assetStatuses)
              AND (ab.business_id IN (:businesses) OR al.location_id IN (:locations) OR alg.location_group_id IN (:locationGroups))
              AND NOT EXISTS (
                SELECT 1
                FROM asset_excluded_location ael
                WHERE ael.asset_id = a.id
                  AND ael.excluded_location_id IN (:locations)
                GROUP BY ael.asset_id
                HAVING COUNT(DISTINCT ael.excluded_location_id) >= :locationsSize
            )
            GROUP BY f.id, f.date_created
            
            ORDER BY name ASC""",
        nativeQuery = true
    )
    suspend fun findFoldersByAuthorOrAssetResources(
        type: String,
        statuses: List<String>,
        assetStatuses: List<String>,
        userId: Long,
        locations: List<Long>,
        businesses: List<Long>,
        locationsSize: Int,
        locationGroups: List<Long>,
        salesPartnerId: Long
    ): List<Folder>

    @Query(
        """SELECT f.*
           FROM folder f
             JOIN asset a on f.id = a.folder_id
             LEFT JOIN asset_location al on a.id = al.asset_id
             LEFT JOIN asset_business ab on a.id = ab.asset_id
             LEFT JOIN asset_location_group alg ON a.id = alg.asset_id
             LEFT JOIN (SELECT asset_id, count(DISTINCT excluded_location_id) as excluded_count
                        FROM asset_excluded_location
                        WHERE excluded_location_id in (:locations)
                        GROUP BY asset_id) ael on a.id = ael.asset_id
    
            WHERE a.sales_partner_id = :salesPartnerId
              AND f.status IN (:statuses)
              AND a.status IN (:assetStatuses)
              AND f.type = :type
              AND (ab.business_id in (:businesses) OR al.location_id in (:locations) OR alg.location_group_id in (:locationGroups))
              AND (ael.excluded_count is null OR ael.excluded_count < :locationsSize)
            GROUP BY f.id, f.date_created
            ORDER BY f.date_created DESC""",
        nativeQuery = true
    )
    suspend fun findFoldersByAssetStatusAndAssetResources(
        type: String,
        statuses: List<String>,
        assetStatuses: List<String>,
        locations: List<Long>,
        businesses: List<Long>,
        locationsSize: Int,
        locationGroups: List<Long>,
        salesPartnerId: Long
    ): List<Folder>

    @Query(
        """SELECT DISTINCT f
           FROM Folder f
            LEFT JOIN f.assets a
            LEFT JOIN a.locations l
            LEFT JOIN a.businesses b
            LEFT JOIN a.locationGroups alg
           WHERE f.status IN (:statuses)
            AND a.salesPartnerId = :salesPartnerId
            AND (l.locationId IN (:locationIds) OR  b.businessId IN (:businessIds) OR alg.locationGroupId IN (:locationGroupIds))
           ORDER BY f.dateCreated DESC""",
        countQuery = """SELECT COUNT(DISTINCT f)
                        FROM Folder f
                            LEFT JOIN f.assets a
                            LEFT JOIN a.locations l
                            LEFT JOIN a.businesses b
                            LEFT JOIN a.locationGroups alg
                        WHERE f.status IN (:statuses)
                        AND a.salesPartnerId = :salesPartnerId
                        AND (l.locationId IN (:locationIds) OR  b.businessId IN (:businessIds) OR alg.locationGroupId IN (:locationGroupIds))"""
    )
    suspend fun findFoldersByAssetResources(
            statuses: List<Folder.Status>,
            locationIds: List<Long>?,
            businessIds: List<Long>,
            locationGroupIds: List<Long>?,
            salesPartnerId: Long,
            pageable: Pageable
    ): Page<Folder>
}
