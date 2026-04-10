package com.phantomshard.keystream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phantomshard.keystream.domain.model.CategoryRef
import com.phantomshard.keystream.domain.model.Service

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val price: Double,
    val stock: Int,
    val maxProfiles: Int,
    val categoryId: String?,
    val categoryName: String?,
    val isPendingCreate: Boolean = false,
    val isPendingUpdate: Boolean = false,
    val isPendingDelete: Boolean = false
) {
    fun toDomain() = Service(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        price = price,
        stock = stock,
        maxProfiles = maxProfiles,
        category = if (categoryId != null && categoryName != null)
            CategoryRef(categoryId, categoryName)
        else null
    )
}

fun Service.toEntity(
    isPendingCreate: Boolean = false,
    isPendingUpdate: Boolean = false,
    isPendingDelete: Boolean = false
) = ServiceEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    price = price,
    stock = stock,
    maxProfiles = maxProfiles,
    categoryId = category?.id,
    categoryName = category?.name,
    isPendingCreate = isPendingCreate,
    isPendingUpdate = isPendingUpdate,
    isPendingDelete = isPendingDelete
)
