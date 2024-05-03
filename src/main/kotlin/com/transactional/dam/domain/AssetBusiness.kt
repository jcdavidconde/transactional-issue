package com.transactional.dam.domain

import java.io.Serializable
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@IdClass(AssetBusinessPK::class)
class AssetBusiness(

        @field: [Id NotNull ManyToOne(optional = false, fetch = FetchType.LAZY)]
    var asset: Asset? = null,

        @field: [Id NotNull]
    var businessId: Long? = 0

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetBusiness

        if (businessId != other.businessId) return false
        if (asset?.id != other.asset?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = businessId.hashCode()
        result = 31 * result + (asset?.id?.hashCode() ?: 0)
        return result
    }
}
