package com.phantomshard.keystream.domain.model

data class Service(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val price: Double,
    val stock: Int,
    val maxProfiles: Int,
    val category: CategoryRef?
)

data class CategoryRef(
    val id: String,
    val name: String
)
