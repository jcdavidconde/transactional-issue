package com.transactional.dam.controller

import com.transactional.dam.model.request.ABEListAssetRequest
import com.transactional.dam.model.request.ABEListFolderRequest
import com.transactional.dam.model.request.ActivateAssetRequest
import com.transactional.dam.model.request.DeactivateAssetRequest
import com.transactional.dam.model.request.DeleteAssetObsoleteLinksRequest
import com.transactional.dam.model.request.ExistAssetRequest
import com.transactional.dam.model.request.GetFolderRequest
import com.transactional.dam.model.request.RequestUtils
import com.transactional.dam.model.response.ABEListAssetResponse
import com.transactional.dam.model.response.ABEListFolderResponse
import com.transactional.dam.model.response.ExistAssetResponse
import com.transactional.dam.model.response.FolderResponse
import com.transactional.dam.model.response.ResponseUtils.toABEResponse
import com.transactional.dam.model.response.ResponseUtils.toResponse
import com.transactional.dam.service.AssetService
import com.transactional.dam.service.FolderService
import com.transactional.dam.task.AssetActivationTask
import com.transactional.dam.task.AssetDeactivationTask
import com.transactional.dam.task.AssetObsoleteLocationLinksDeletionTask
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.RequestBean
import io.micronaut.scheduling.TaskScheduler
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import java.time.Duration
import jakarta.validation.Valid

@Controller("internal/dam")
@Tag(name = "Internal Controller")
@Validated
class InternalController(
        @Inject private val folderService: FolderService,
        @Inject private val assetService: AssetService,
        @Inject private val monolithClient: com.transactional.dam.http.monolith.MonolithClient,
        @Inject private val taskScheduler: TaskScheduler,
        @Value("\${cronjob.deleteAssetObsoleteLinks.assetPageSize}") private val assetPageSize: Int
) {

    @Post("/folders")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = ABEListFolderRequest::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun listFolders(
        @Valid @Body
        request: ABEListFolderRequest
    ): ABEListFolderResponse {
        val pageable = RequestUtils.convert(request.offset, request.max)
        return folderService.listFolders(
            request.locationIds,
            request.businessId,
            request.locationGroupIds,
            request.salesPartnerId,
            pageable
        ).toABEResponse(request)
    }

    @Post("/assets")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = ABEListAssetRequest::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun listAssets(
        @Valid @Body
        request: ABEListAssetRequest
    ): ABEListAssetResponse {
        val pageable = RequestUtils.convert(request.offset, request.max)
        return assetService.listAssets(
            request.folderId,
            request.locationIds,
            listOf(request.businessId),
            request.locationGroupIds,
            request.query,
            request.salesPartnerId,
            pageable
        ).toABEResponse(request)
    }

    @Post("/assets/activation")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun activateAssets(
        @Valid @Body
        request: ActivateAssetRequest
    ): HttpResponse<Nothing> {
        taskScheduler.schedule(Duration.ofSeconds(0), AssetActivationTask(assetService, request.startDate))
        return HttpResponse.accepted()
    }

    @Post("/assets/deactivation")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun deactivateAssets(
        @Valid @Body
        request: DeactivateAssetRequest
    ): HttpResponse<Nothing> {
        taskScheduler.schedule(Duration.ofSeconds(0), AssetDeactivationTask(assetService, request.endDate))
        return HttpResponse.accepted()
    }

    @Post("/assets/migration")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun existAssets(
        @Valid @Body
        request: ExistAssetRequest
    ): ExistAssetResponse {
        return ExistAssetResponse(assetService.findExistingTemplateIds(request.templateIds))
    }

    @Get("/folders/migration")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun getFolder(
        @Valid @RequestBean
        request: GetFolderRequest
    ): FolderResponse? {
        val folder = folderService.findByNameAndAuthorId(request.name, request.authorId)
        return folder?.toResponse()
    }

    @Post("/assets/obsolete-location-deletion")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Accepted"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    suspend fun deleteAssetObsoleteLocationLinks(
        @Valid @Body
        request: DeleteAssetObsoleteLinksRequest
    ): HttpResponse<Nothing> {
        taskScheduler.schedule(
            Duration.ofSeconds(0),
            AssetObsoleteLocationLinksDeletionTask(
                assetService,
                monolithClient,
                assetPageSize,
                request.salesPartnerIds?.toSet()
            )
        )
        return HttpResponse.accepted()
    }
}
