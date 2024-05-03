package com.transactional.dam.http.monolith.model

data class ProductPlan(val features: List<String>) {
    enum class Feature {
        DAM
    }

    companion object {
        val features: List<String> = listOf(Feature.DAM.name)
    }
}
