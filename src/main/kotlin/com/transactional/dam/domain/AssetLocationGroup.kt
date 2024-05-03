package com.transactional.dam.domain

import java.io.Serializable
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@IdClass(AssetLocationGroupPK::class)
class AssetLocationGroup(

        @field: [Id NotNull ManyToOne(optional = false, fetch = FetchType.LAZY)]
    var asset: Asset? = null,

        @field: [Id NotNull]
    var locationGroupId: Long? = 0

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetLocationGroup

        if (locationGroupId != other.locationGroupId) return false
        if (asset?.id != other.asset?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locationGroupId.hashCode()
        result = 31 * result + (asset?.id?.hashCode() ?: 0)
        return result
    }
}
