package com.transactional.dam.domain

import com.transactional.dam.model.request.CreateFolderRequest
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
class Folder {

    @field: [Id GeneratedValue(strategy = GenerationType.IDENTITY)]
    var id: Long = 0

    @field: [NotBlank Size(min = 1, max = 60)]
    var name: String = ""

    @field: [Size(max = 250)]
    var description: String? = null

    @field: [DateCreated NotNull]
    var dateCreated: LocalDateTime = LocalDateTime.now()

    @field: [DateUpdated NotNull]
    var dateUpdated: LocalDateTime = LocalDateTime.now()

    @field: [NotNull Enumerated(EnumType.STRING)]
    var status: Status = Status.VISIBLE

    @field: [NotNull Enumerated(EnumType.STRING)]
    var type: Type = Type.SOCIAL_POST_TEMPLATE

    @field: [NotNull]
    var authorId: Long = 0

    @field: [OneToMany(mappedBy = "folder")]
    var assets: Set<Asset> = emptySet()

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

        fun create(request: CreateFolderRequest): Folder {
            val folder = Folder()

            folder.name = request.name
            folder.description = request.description
            folder.type = request.type
            folder.status = request.status
            folder.authorId = request.authorId

            return folder
        }
    }
}
