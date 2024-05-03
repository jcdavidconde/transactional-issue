package com.transactional.dam.service.user

data class UserForbiddenException(override val message: String) : Throwable(message)
