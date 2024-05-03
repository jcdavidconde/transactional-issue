package com.transactional.dam.extension

import com.transactional.dam.ApplicationKt
import com.transactional.dam.config.AdditionalConfiguration

private val additionalConfiguration: AdditionalConfiguration = ApplicationKt.context.getBean(AdditionalConfiguration::class.java)

/* Extension methods */

fun Map<*, *>.maskSensitiveData(): Map<String, *> {
    if (this.isEmpty()) {
        emptyMap<String, Any>()
    }
    return maskDataRecursively(this.mapKeys { it.key.toString() })
}

/* Helper methods */

/**
 * Dev (Jan) comment:
 * Really basic implementation for masking sensitive data, most likely there is a smoother way of doing it, but for now
 * it fulfills its purpose and can be improved later (if needed)
 * Example: [password: "secret"] --> [password: "Filtered#String"]
 */
private fun maskDataRecursively(map: Map<String, *>): Map<String, *> {
    val newMap: MutableMap<String, Any?> = emptyMap<String, Any?>().toMutableMap()
    map.forEach { entry ->
        if (additionalConfiguration.sensitiveDataBlacklistedWords.any { word: String -> entry.key.lowercase().contains(word.lowercase()) }) {
            val classType: String = entry.value?.javaClass?.simpleName ?: "unknown"
            newMap[entry.key] = "[Filtered#${ classType }]"
        } else if (entry.value is Map<*, *>) {
            newMap[entry.key] = maskDataRecursively((entry.value as Map<*, *>).mapKeys { it.key.toString() })
        } else if (entry.value is List<*>) {
            val newList = mutableListOf<Any?>()
            (entry.value as List<*>).forEach { listElement ->
                if (listElement is Map<*, *>) {
                    newList.add(maskDataRecursively(listElement.mapKeys { it.key.toString() }))
                } else {
                    newList.add(listElement)
                }
            }
            newMap[entry.key] = newList
        } else {
            newMap[entry.key] = entry.value
        }
    }

    return newMap
}
