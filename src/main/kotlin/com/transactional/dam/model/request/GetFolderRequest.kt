package com.transactional.dam.model.request

import io.micronaut.core.annotation.Introspected
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Introspected
data class GetFolderRequest(
    @field: [NotNull NotBlank]
    val name: String,

    @field: [NotNull Positive]
    val authorId: Long
)
