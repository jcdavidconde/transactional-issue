package com.transactional.dam.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.transactional.dam.domain.Folder
import java.time.LocalDateTime

data class FolderResponse(

        val id: Long,

        val name: String,

        val description: String?,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateCreated: LocalDateTime,

        @field: [JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")]
    val dateUpdated: LocalDateTime,

        val status: Folder.Status,

        val type: Folder.Type,

        val authorId: Long,

        val numVisibleAssets: Long = 0,

        val numTotalAssets: Long = 0
)
