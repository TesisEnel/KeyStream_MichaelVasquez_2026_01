package com.phantomshard.keystream.data.remote.dto

import com.phantomshard.keystream.data.local.entity.CategoryEntity
import com.phantomshard.keystream.domain.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoriesResponseDto(val data: CategoriesDataDto)

@Serializable
data class CategoriesDataDto(
    val categories: List<CategoryDto>,
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String? = null
) {
    fun toDomain() = Category(id, name, description)
    fun toEntity() = CategoryEntity(id, name, description)
}

@Serializable
data class DeleteCategoryRequest(
    val id: String
)
