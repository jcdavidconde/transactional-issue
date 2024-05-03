package com.transactional.dam.controller

import com.transactional.dam.model.User
import com.transactional.dam.model.request.CreateFolderRequest
import com.transactional.dam.model.request.ListFolderRequest
import com.transactional.dam.model.request.headers.RequestHeaders
import com.transactional.dam.model.request.selection.resources.ManagedResourcesSelection
import com.transactional.dam.model.response.FolderResponse
import com.transactional.dam.model.response.ListFolderResponse
import com.transactional.dam.model.response.ResponseUtils.toResponse
import com.transactional.dam.service.AssetService
import com.transactional.dam.service.FolderService
import com.transactional.dam.service.user.UserAuthorisationService
import com.transactional.dam.service.user.UserManagedResourcesService
import com.transactional.dam.service.user.UserService
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.RequestBean
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid

@Controller("/api/dam/folders")
@Tag(name = "Folder Controller")
@Validated
class FolderController(
        @Inject private val folderService: FolderService,
        @Inject private val assetService: AssetService,
        @Inject private val userService: UserService,
        @Inject private val userAuthorisationService: UserAuthorisationService,
        @Inject private val userManagedResourcesService: UserManagedResourcesService
) {

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
                            schema = (Schema(implementation = FolderResponse::class))
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
        request: CreateFolderRequest
    ): FolderResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        return folderService.createFolder(request).toResponse(0, 0)
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
                            schema = (Schema(implementation = FolderResponse::class))
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
    ): FolderResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))

        val folder = folderService.getFolder(id)
        val applicableManagedResources = userManagedResourcesService.getManagedResources(user)
        userAuthorisationService.canAccessFolder(user, folder, applicableManagedResources)

        val counts = assetService.countAssets(id, applicableManagedResources)
        return folder.toResponse(counts.visible, counts.total)
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
                            schema = (Schema(implementation = ListFolderResponse::class))
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
        request: ListFolderRequest
    ): ListFolderResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        val selectedManagedResources = ManagedResourcesSelection.from(request)

        val applicableManagedResources = if (selectedManagedResources == null) {
            userManagedResourcesService.getManagedResources(user)
        } else {
            userManagedResourcesService.getFilteredListResources(user, selectedManagedResources)
        }

        val folders = folderService.listFolders(user, request, applicableManagedResources)
        return ListFolderResponse(
            folders.map {
                val counts = assetService.countAssets(it.id, applicableManagedResources)
                it.toResponse(counts.visible, counts.total)
            }
        )
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
                            schema = (Schema(implementation = FolderResponse::class))
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
        updateFolderRequest: com.transactional.dam.model.request.UpdateFolderRequest
    ): FolderResponse {
        val user = userService.getUser(RequestHeaders.from(httpHeaders))
        userAuthorisationService.needsFeature(user, User.Feature.DAM)

        val applicableManagedResources = userManagedResourcesService.getManagedResources(user)
        userAuthorisationService.canAccessFolder(user, id, applicableManagedResources)

        val folder = folderService.updateFolder(id, updateFolderRequest)
        val counts = assetService.countAssets(id, applicableManagedResources)
        return folder.toResponse(counts.visible, counts.total)
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
        userAuthorisationService.canDeleteFolder(user, id, applicableManagedResources)

        folderService.deleteFolder(id)
        return HttpResponse.ok()
    }
}
