package com.transactional.dam.domain

import java.io.Serializable
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull

data class AssetLocationPK(

        @field: [NotNull ManyToOne(optional = false, fetch = FetchType.LAZY)]
    var asset: Asset? = null,

        @field: [NotNull]
    var locationId: Long? = 0

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetLocationPK

        if (locationId != other.locationId) return false
        if (asset?.id != other.asset?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locationId.hashCode()
        result = 31 * result + (asset?.id?.hashCode() ?: 0)
        return result
    }
}
