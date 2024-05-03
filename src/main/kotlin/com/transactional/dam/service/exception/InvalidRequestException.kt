package com.transactional.dam.service.exception

data class InvalidRequestException(override val message: String) : Throwable(message)
