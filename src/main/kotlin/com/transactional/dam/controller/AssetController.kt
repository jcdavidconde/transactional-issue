package com.transactional.dam.controller

import com.transactional.dam.domain.Asset
import com.transactional.dam.model.User
import com.transactional.dam.model.request.*
import com.transactional.dam.model.request.headers.RequestHeaders
import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import com.transactional.dam.model.response.AssetResponse
import com.transactional.dam.model.response.EnrichedAssetResponse
import com.transactional.dam.model.response.ListEnrichedAssetResponse
import com.transactional.dam.model.response.ResponseUtils.toResponse
import com.transactional.dam.service.AssetService
import com.transactional.dam.service.user.UserAuthorisationService
import com.transactional.dam.service.user.UserManagedResourcesService
import com.transactional.dam.service.user.UserService
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid

@Controller("api/dam/assets")
@Tag(name = "Asset Controller")
@Validated
class AssetController(
        @Inject private val assetService: AssetService,
        @Inject private val userService: UserService,
        @Inject private val userAuthorisationService: UserAuthorisationService,
        @Inject private val userManagedResourcesService: UserManagedResourcesService
) {

    @Get("/types")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
            Content(
                mediaType = "application/json",
                schema = (Schema(implementation = Array<Asset.Type>::class))
            )
        ]
    )
    fun getAssetTypes(): Array<Asset.Type> {
        return assetService.retrieveAssetTypes()
    }

    @Post
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = AssetResponse::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    suspend fun create(
        httpHeaders: HttpHeaders,
        @Body @Valid
        request: CreateAssetRequest
    ): AssetResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        val selectedManagedResources = userManagedResourcesService.getManagedResources(
            user,
            ManagedResourcesSelection.from(request)
        )
        val managedResources = userManagedResourcesService.getManagedResources(user)

        userAuthorisationService.canAccessFolder(user, request.folderId, managedResources)
        return assetService.createAsset(user, request, selectedManagedResources).toResponse()
    }

    @Post(uri = "/{id}/increment-usage-count")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not Found")
        ]
    )
    suspend fun incrementUsageCount(
        httpHeaders: HttpHeaders,
        id: Long
    ): HttpResponse<Nothing> {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        val applicableManagedResources = userManagedResourcesService.getManagedResources(user)
        val asset = assetService.getAsset(id, applicableManagedResources)
        userAuthorisationService.canAccessAsset(user, asset, applicableManagedResources)
        assetService.incrementUsageCount(id)

        return HttpResponse.ok()
    }

    @Get(uri = "/{id}")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = EnrichedAssetResponse::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not Found")
        ]
    )
    suspend fun get(
        httpHeaders: HttpHeaders,
        id: Long
    ): EnrichedAssetResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))

        val applicableManagedResources = userManagedResourcesService.getManagedResources(user)
        val asset = assetService.getAsset(id, applicableManagedResources)
        userAuthorisationService.canAccessAsset(user, asset, applicableManagedResources)
        return assetService.getEnrichedAsset(user, asset)
    }

    @Get
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = ListEnrichedAssetResponse::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    suspend fun list(
        httpHeaders: HttpHeaders,
        @Valid @RequestBean
        request: ListAssetRequest
    ): ListEnrichedAssetResponse {
        validateRequiredParams(request)
        val user = userService.getUser(RequestHeaders.from(httpHeaders))

        val selectedManagedResources = ManagedResourcesSelection.from(request)
        val applicableManagedResources = if (selectedManagedResources == null) {
            userManagedResourcesService.getManagedResources(user)
        } else {
            userManagedResourcesService.getFilteredListResources(user, selectedManagedResources)
        }

        return assetService.listEnrichedAssets(user, request, applicableManagedResources)
    }

    fun validateRequiredParams(listAssetRequest: ListAssetRequest) {
        if (listAssetRequest.folderIds.isNullOrEmpty() && listAssetRequest.type == null) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "Either type or folder_ids is required")
        }
    }

    @Patch(uri = "/{id}")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = (Schema(implementation = AssetResponse::class))
                        )
                        )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not Found")
        ]
    )
    suspend fun update(
        httpHeaders: HttpHeaders,
        id: Long,
        @Body @Valid
        updateAssetRequest: UpdateAssetRequest
    ): AssetResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        val updateResourcesSelection = ManagedResourcesSelection.from(updateAssetRequest)
        val selectedManagedResources = userManagedResourcesService.getManagedResources(user, updateResourcesSelection)
        val managedResources = userManagedResourcesService.getManagedResources(user)
        val assetToAccess = assetService.getAsset(id)

        userAuthorisationService.canAccessAsset(user, assetToAccess, managedResources)
        userAuthorisationService.canUpdateOrDeleteAsset(user, assetToAccess, managedResources)

        val asset = assetService.updateAsset(id, updateAssetRequest, selectedManagedResources, managedResources)
        return asset.toResponse()
    }

    @Delete(uri = "/{id}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "400", description = "Bad Request"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
            ApiResponse(responseCode = "404", description = "Not Found")
        ]
    )
    suspend fun delete(
        httpHeaders: HttpHeaders,
        id: Long
    ): HttpResponse<Nothing> {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        val applicableManagedResources = userManagedResourcesService.getManagedResources(user)
        val assetToAccess = assetService.getAsset(id)

        userAuthorisationService.canAccessAsset(user, assetToAccess, applicableManagedResources)
        userAuthorisationService.canUpdateOrDeleteAsset(user, assetToAccess, applicableManagedResources)

        assetService.deleteAsset(user, id)
        return HttpResponse.ok()
    }
}
