package com.transactional.dam.model.request.headers

data class MissingHeaderException(val header: String) : Throwable("Missing required header: $header")
