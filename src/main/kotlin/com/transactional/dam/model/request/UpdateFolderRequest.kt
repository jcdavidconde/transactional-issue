package com.transactional.dam.model.request

import com.transactional.dam.domain.Folder
import jakarta.validation.constraints.Size

data class UpdateFolderRequest(

    @field: [Size(min = 1, max = 60)]
    val name: String? = null,

    @field: [Size(max = 250)]
    val description: String? = null,

    val status: Folder.Status? = null
)
