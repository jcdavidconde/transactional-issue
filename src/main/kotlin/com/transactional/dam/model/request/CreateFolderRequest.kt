package com.transactional.dam.model.request

import com.transactional.dam.domain.Folder
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateFolderRequest(

        @field: [NotBlank Size(min = 1, max = 60)]
    val name: String,

        @field: [Size(max = 250)]
    val description: String?,

        val type: Folder.Type,

        val status: Folder.Status = Folder.Status.VISIBLE,

        @field: [NotNull Positive]
    val authorId: Long
)
