package com.transactional.dam.domain

import java.io.Serializable
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull

data class AssetExcludedLocationPK(

        @field: [NotNull ManyToOne(optional = false, fetch = FetchType.LAZY)]
    var asset: Asset? = null,

        @field: [NotNull]
    var excludedLocationId: Long? = 0

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetExcludedLocationPK

        if (excludedLocationId != other.excludedLocationId) return false
        if (asset?.id != other.asset?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = excludedLocationId.hashCode()
        result = 31 * result + (asset?.id?.hashCode() ?: 0)
        return result
    }
}
