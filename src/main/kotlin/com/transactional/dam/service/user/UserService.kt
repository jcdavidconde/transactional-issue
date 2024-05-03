package com.transactional.dam.service.user

import com.transactional.dam.model.User
import com.transactional.dam.model.request.headers.RequestHeaders
import jakarta.inject.Singleton

@Singleton
class UserService {

    fun getUser(headers: RequestHeaders): User {
        return User(
            headers.userId,
            headers.salesPartnerId,
            headers.userRole,
            headers.userFeatures,
            headers.accessToken
        )
    }
}
