package com.transactional.dam.service.exception

data class NotFoundException(val item: String, val id: Long) : Throwable("$item with id $id not found")
