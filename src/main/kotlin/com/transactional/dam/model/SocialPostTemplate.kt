package com.transactional.dam.model

data class SocialPostTemplate(
        val id: Long,
        val isStoreLocator: Boolean? = false,
        val postType: String? = null,
        val author: com.transactional.dam.model.Author? = null,
        val directories: List<String>? = null,
        val photos: List<String>? = null,
        val videos: List<String>? = null
)
