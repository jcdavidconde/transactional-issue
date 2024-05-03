package com.transactional.dam.http.monolith

data class MonolithClientException(val callInfo: String, val task: String, val error: String?) :
        Throwable("Failed to $task for $callInfo: $error")
