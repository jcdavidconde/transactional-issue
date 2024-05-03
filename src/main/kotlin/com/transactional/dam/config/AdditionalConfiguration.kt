package com.transactional.dam.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("additional")
class AdditionalConfiguration {
    var sensitiveDataBlacklistedWords: List<String> = emptyList()
}
