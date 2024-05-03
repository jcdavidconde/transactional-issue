package com.transactional.dam.model.request

import io.micronaut.data.model.Pageable

object RequestUtils {

    fun convert(offset: Int, max: Int): Pageable {
        // Find the smallest page size that will return all requested items in one page
        // while minimizing the number of items retrieved that are not requested.
        // Best case: page size = max, worst case: page size = offset + max
        for (window in max until offset + max) {
            // With each new window tried, the window's first start index is at the requested offset.
            // If the try for a start index fails, the window is shifted to the left if window is larger than the max.
            for (leftShift in 0..window - max) {
                // Check if the window can be the page size at the current window start index:
                // the number of items before the window start index needs to be divisible by the window size
                if ((offset - leftShift) % window == 0) {
                    val page = (offset - leftShift) / window
                    return Pageable.from(page, window)
                }
            }
        }
        return Pageable.from(0, offset + max)
    }

    fun formatSearchQuery(input: String?): String {
        if (input.isNullOrBlank()) {
            return ""
        }
        val regex = """['"@\(\)&+\-<>*~]""".toRegex()
        val sanitizedInput = regex.replace(input, " ")
        val trimmedInput = sanitizedInput.trim()
        val keywords = trimmedInput.split(" ")
        val nonEmptyKeywords = keywords.filter { it.isNotBlank() }
        return nonEmptyKeywords.joinToString(" ") { "$it*" }
    }
}
