package com.transactional.dam.model.request.headers

import com.transactional.dam.model.User
import io.micronaut.http.HttpHeaders

data class RequestHeaders(
    val accessToken: String,
    val userId: Long,
    val salesPartnerId: Long,
    val userRole: User.Role,
    val userFeatures: Set<User.Feature>
) {
    companion object {

        fun from(httpHeaders: HttpHeaders): RequestHeaders {
            return RequestHeaders(
                accessToken(httpHeaders["X-Uberall-Access-Token"]),
                userId(httpHeaders["X-Uberall-User-ID"]),
                salesPartnerId(httpHeaders["X-Uberall-Sales-Partner-ID"]),
                userRole(httpHeaders["X-Uberall-User-Role"]),
                userFeatures(httpHeaders["X-Uberall-User-Features"])
            )
        }

        private fun accessToken(header: String?): String {
            return header ?: throw MissingHeaderException("X-Uberall-Access-Token")
        }

        private fun userId(header: String?): Long {
            return header?.toLong() ?: throw MissingHeaderException("X-Uberall-User-ID")
        }

        private fun salesPartnerId(header: String?): Long {
            return header?.toLong() ?: throw MissingHeaderException("X-Uberall-Sales-Partner-ID")
        }

        private fun userRole(header: String?): User.Role {
            return if (header != null) User.Role.valueOf(header) else throw MissingHeaderException("X-Uberall-User-Role")
        }

        private fun userFeatures(header: String?): Set<User.Feature> {
            if (header == null) {
                throw MissingHeaderException("X-Uberall-User-Features")
            }

            val features = header.split(",")
            return User.Feature.values().filter { features.contains(it.name) }.toSet()
        }
    }
}
