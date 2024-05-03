package com.transactional.dam.domain

import com.transactional.dam.model.User
import com.transactional.dam.model.request.CreateAssetRequest
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
class Asset {

    @field: [Id GeneratedValue(strategy = GenerationType.IDENTITY)]
    var id: Long = 0

    @field: [NotBlank Size(min = 1, max = 250)]
    var name: String = ""

    @field: [Size(max = 4096)]
    var description: String? = null

    @field: [DateCreated NotNull]
    var dateCreated: LocalDateTime = LocalDateTime.now()

    @field: [DateUpdated NotNull]
    var dateUpdated: LocalDateTime = LocalDateTime.now()

    @field: [NotNull Enumerated(EnumType.STRING)]
    var status: Status = Status.VISIBLE

    @field: [NotNull]
    var startDate: LocalDateTime = LocalDateTime.now()

    var endDate: LocalDateTime? = null

    @field: [NotNull Enumerated(EnumType.STRING)]
    var type: Type = Type.SOCIAL_POST_TEMPLATE

    @field: [NotNull]
    var authorId: Long = 0

    @field: [NotNull ManyToOne(optional = false, fetch = FetchType.LAZY)]
    var folder: Folder? = null

    @field: [NotNull]
    var salesPartnerId: Long = 0

    @field: [NotNull]
    var usageCount: Int = 0

    @field: [OneToMany(mappedBy = "asset", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])]
    var locations: Set<AssetLocation> = emptySet()

    @field: [OneToMany(mappedBy = "asset", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])]
    var businesses: Set<AssetBusiness> = emptySet()

    @field: [OneToMany(mappedBy = "asset", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])]
    var excludedLocations: Set<AssetExcludedLocation> = emptySet()

    @field:[OneToMany(mappedBy = "asset", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])]
    var locationGroups: Set<AssetLocationGroup> = emptySet()

    @field: [NotNull]
    var templateId: Long = 0

    enum class Status {
        HIDDEN,
        VISIBLE,
        REMOVED;

        companion object {
            val nonRemoved: List<Status> = listOf(HIDDEN, VISIBLE)
        }
    }

    enum class Type {
        AD_TEMPLATE,
        SOCIAL_POST_TEMPLATE
    }

    companion object {

        fun create(user: User, request: CreateAssetRequest, folder: Folder): Asset {
            val asset = Asset()

            asset.name = request.name
            asset.description = request.description
            asset.type = request.type
            asset.status = request.status
            asset.startDate = request.startDate
            asset.endDate = request.endDate
            asset.authorId = request.authorId
            asset.folder = folder
            asset.salesPartnerId = user.salesPartnerId
            asset.templateId = request.templateId

            return asset
        }
    }
}
