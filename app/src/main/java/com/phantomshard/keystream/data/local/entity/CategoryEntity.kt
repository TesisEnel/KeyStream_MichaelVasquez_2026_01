package com.phantomshard.keystream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phantomshard.keystream.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val isPendingCreate: Boolean = false,
    val isPendingUpdate: Boolean = false,
    val isPendingDelete: Boolean = false
) {
    fun toDomain() = Category(id, name, description)
}

fun Category.toEntity(
    isPendingCreate: Boolean = false,
    isPendingUpdate: Boolean = false,
    isPendingDelete: Boolean = false
) = CategoryEntity(id, name, description, isPendingCreate, isPendingUpdate, isPendingDelete)
