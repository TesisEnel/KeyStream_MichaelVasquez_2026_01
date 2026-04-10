package com.phantomshard.keystream.data.remote.dto

import com.phantomshard.keystream.data.local.entity.ServiceEntity
import com.phantomshard.keystream.domain.model.CategoryRef
import com.phantomshard.keystream.domain.model.Service
import kotlinx.serialization.Serializable

@Serializable
data class ServicesResponseDto(val data: ServicesDataDto)

@Serializable
data class ServicesDataDto(
    val services: List<ServiceDto>,
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1
)

@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val price: Double = 0.0,
    val stock: Int = 0,
    val maxProfiles: Int = 1,
    val category: CategoryRefDto? = null
) {
    fun toDomain() = Service(id, name, description, imageUrl, price, stock, maxProfiles, category?.toDomain())
    fun toEntity() = ServiceEntity(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        price = price,
        stock = stock,
        maxProfiles = maxProfiles,
        categoryId = category?.id,
        categoryName = category?.name
    )
}

@Serializable
data class CategoryRefDto(
    val id: String = "",
    val name: String = ""
) {
    fun toDomain() = CategoryRef(id, name)
}

@Serializable
data class DeleteServiceRequest(
    val id: String
)
