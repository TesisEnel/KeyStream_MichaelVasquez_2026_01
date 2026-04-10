package com.phantomshard.keystream.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val id: String,
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class CreateServiceRequest(
    val name: String,
    val categoryId: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val maxProfiles: Int? = null
)

@Serializable
data class UpdateServiceRequest(
    val id: String,
    val name: String? = null,
    val categoryId: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val maxProfiles: Int? = null
)
