package com.transactional.dam.model.response

data class ABEListFolderResponse(
    val folders: List<ABEFolderResponse>,
    val offset: Int,
    val max: Int,
    val totalCount: Long
)
