package com.transactional.dam.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("micronaut.server")
class ServerConfiguration {
    var host: String? = "localhost"
    var port: String? = null
}
