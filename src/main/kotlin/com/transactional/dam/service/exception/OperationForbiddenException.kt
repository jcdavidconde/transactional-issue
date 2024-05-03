package com.transactional.dam.service.exception

import com.transactional.dam.model.User

data class OperationForbiddenException(val user: User, val operation: String, val error: String) :
    Throwable("User ${user.id} cannot perform $operation: $error")
